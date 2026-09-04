import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, firstValueFrom, map, Observable, of, startWith, switchMap, tap } from 'rxjs';

import {
  ExecutionApiService,
  ExecutionResult,
  CompletionResult
} from '../../core/api/execution-api.service';
import { LabApiService } from '../../core/api/lab-api.service';
import { TutorApiService } from '../../core/api/tutor-api.service';
import { DraftStoreService } from '../../core/storage/draft-store.service';
import { KeyConcept, LabContent } from './lab.model';
import { CodeEditorComponent } from '../../shared/code-editor/code-editor.component';
import { EXECUTION_AVAILABLE } from '../../core/api/api-config';

type LabViewState =
  | { status: 'loading' }
  | { status: 'loaded'; lab: LabContent }
  | { status: 'error'; message: string };

@Component({
  selector: 'dlr-lab-workspace',
  imports: [AsyncPipe, CodeEditorComponent],
  templateUrl: './lab-workspace.component.html',
  styleUrl: './lab-workspace.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LabWorkspaceComponent {
  readonly executionAvailable = signal(false);
  readonly executionChecking = signal(EXECUTION_AVAILABLE);
  readonly executionMessage = signal(
    EXECUTION_AVAILABLE ? 'Vérification du Runner local…' : "L'exécution est désactivée pour ce déploiement."
  );
  private readonly route = inject(ActivatedRoute);
  private readonly labApi = inject(LabApiService);
  private readonly executionApi = inject(ExecutionApiService);
  private readonly tutorApi = inject(TutorApiService);
  private readonly drafts = inject(DraftStoreService);

  readonly code = signal('');
  readonly running = signal(false);
  readonly execution = signal<ExecutionResult | null>(null);
  readonly executionError = signal<string | null>(null);
  readonly quizValues = signal<Record<string, number | string>>({});
  readonly checklist = signal<boolean[]>([]);
  readonly completing = signal(false);
  readonly completion = signal<CompletionResult | null>(null);
  readonly completionError = signal<string | null>(null);
  readonly sourceOrigin = signal<'EDITOR' | 'PASTE' | 'IMPORT'>('EDITOR');
  readonly resumed = signal(false);
  readonly tutorAvailable = signal(false);
  readonly tutorModel = signal('Ollama');
  readonly tutorLoading = signal(false);
  readonly tutorResponse = signal<string | null>(null);
  readonly tutorError = signal<string | null>(null);
  readonly hintLevel = signal(0);
  readonly resetting = signal(false);
  readonly resetNotice = signal<string | null>(null);

  private attemptId: string | null = null;
  private activeLabCode = 'JAVA-01';
  private activeLanguage = 'JAVA';
  private draftTimer: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    this.refreshRunnerStatus();
    this.tutorApi.status().subscribe({
      next: (status) => { this.tutorAvailable.set(status.available); this.tutorModel.set(status.selectedModel); },
      error: () => this.tutorAvailable.set(false)
    });
  }

  refreshRunnerStatus(): void {
    if (!EXECUTION_AVAILABLE) {
      this.executionAvailable.set(false);
      this.executionChecking.set(false);
      return;
    }
    this.executionChecking.set(true);
    this.executionApi.status().subscribe({
      next: (status) => {
        this.executionAvailable.set(status.available);
        this.executionMessage.set(status.message);
        this.executionChecking.set(false);
      },
      error: () => {
        this.executionAvailable.set(false);
        this.executionMessage.set("L'API locale est hors ligne ou inaccessible depuis cet appareil.");
        this.executionChecking.set(false);
      }
    });
  }

  pathLabel(code: string): string {
    if (code.startsWith('SPRING_BOOT-')) return 'SPRING BOOT';
    if (code.startsWith('TYPESCRIPT-')) return 'TYPESCRIPT';
    if (code.startsWith('ANGULAR-')) return 'ANGULAR';
    if (code.startsWith('DEVOPS-')) return 'DOCKER & CI/CD';
    if (code.startsWith('ARCHITECTURE-')) return 'ARCHITECTURE SYSTÈME';
    if (code.startsWith('SQL-')) return 'SQL';
    if (code.startsWith('PYTHON-')) return 'PYTHON';
    if (code.startsWith('LLM-')) return 'LEARN LLMS';
    return 'JAVA';
  }

  readonly state$: Observable<LabViewState> = this.route.paramMap.pipe(
    map((params) => params.get('code') ?? 'JAVA-01'),
    switchMap((code) =>
      this.labApi.getLab(code).pipe(
        switchMap((lab) => this.executionApi.currentWorkspace(lab.code).pipe(
          tap((workspace) => {
          this.activeLabCode = lab.code;
          this.activeLanguage = lab.language;
          this.attemptId = workspace?.attempt.id ?? null;
          this.execution.set(null);
          this.executionError.set(null);
          this.quizValues.set(Object.fromEntries((workspace?.quizAnswers ?? []).map((answer) => [
            answer.questionId,
            answer.selectedChoice ?? answer.answerText ?? ''
          ])));
          this.checklist.set(workspace?.checklist.length === lab.checklist.length
            ? workspace.checklist
            : lab.checklist.map(() => false));
          this.completion.set(null);
          this.completionError.set(null);
          this.resetNotice.set(null);
          this.code.set(workspace?.sourceCode ?? lab.exercises[0]?.starterCode ?? '');
          this.sourceOrigin.set(workspace?.sourceOrigin ?? 'EDITOR');
          this.resumed.set(workspace !== null);
          if (workspace === null) {
            void this.restoreLocalDraft(lab.code);
          } else {
            void this.drafts.remove(lab.code);
          }
        }),
          map(() => lab)
        )),
        map((lab) => ({ status: 'loaded', lab }) as const),
        startWith({ status: 'loading' } as const),
        catchError(() =>
          of({
            status: 'error',
            message: "Le laboratoire n'a pas pu être chargé. Vérifie que l'API DLR est démarrée."
          } as const)
        )
      )
    )
  );

  onCodeChange(value: string): void {
    this.code.set(value);
    this.sourceOrigin.set('EDITOR');
    this.scheduleDraft();
  }

  async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;
    const extension = this.fileExtension(this.activeLanguage);
    if (!file.name.toLowerCase().endsWith(extension)) {
      this.executionError.set(`Seuls les fichiers source ${extension} sont acceptés.`);
      return;
    }
    if (file.size > 65_536) {
      this.executionError.set('Le fichier dépasse la limite de 64 Kio.');
      return;
    }
    this.code.set(await file.text());
    this.sourceOrigin.set('IMPORT');
    this.executionError.set(null);
    this.scheduleDraft();
  }

  async runCode(): Promise<void> {
    if (!this.executionAvailable() || this.running() || this.code().trim().length === 0) {
      return;
    }

    this.running.set(true);
    this.execution.set(null);
    this.executionError.set(null);
    try {
      const attemptId = await this.ensureAttempt();
      const submission = await firstValueFrom(
        this.executionApi.submit(attemptId, this.code(), this.activeLanguage, this.sourceOrigin())
      );
      const result = await firstValueFrom(this.executionApi.run(submission.id));
      this.execution.set(result);
    } catch (error) {
      this.executionError.set(this.errorMessage(error));
    } finally {
      this.running.set(false);
    }
  }

  onQuizChoice(questionId: string, choice: number): void {
    this.quizValues.update((values) => ({ ...values, [questionId]: choice }));
  }

  onQuizText(questionId: string, event: Event): void {
    const value = (event.target as HTMLTextAreaElement).value;
    this.quizValues.update((values) => ({ ...values, [questionId]: value }));
  }

  onChecklistChange(index: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.checklist.update((items) => items.map((item, itemIndex) => itemIndex === index ? checked : item));
  }

  async completeLab(lab: LabContent): Promise<void> {
    if (this.completing() || this.completion() !== null) {
      return;
    }
    this.completing.set(true);
    this.completionError.set(null);
    try {
      const attemptId = await this.ensureAttempt();
      for (const question of lab.quiz) {
        const value = this.quizValues()[question.code];
        if (question.type === 'SINGLE_CHOICE' && typeof value !== 'number') {
          throw new Error(`Réponds à la question « ${question.prompt} ».`);
        }
        if (question.type === 'FREE_TEXT' && (typeof value !== 'string' || value.trim().length === 0)) {
          throw new Error(`Rédige une réponse pour « ${question.prompt} ».`);
        }
        await firstValueFrom(this.executionApi.answerQuiz(
          attemptId,
          question.code,
          question.type === 'SINGLE_CHOICE' ? { selectedChoice: value as number } : { answerText: value as string }
        ));
      }
      await firstValueFrom(this.executionApi.saveChecklist(attemptId, this.checklist()));
      this.completion.set(await firstValueFrom(this.executionApi.complete(attemptId)));
    } catch (error) {
      this.completionError.set(this.errorMessage(error));
    } finally {
      this.completing.set(false);
    }
  }

  async continueBelowThreshold(): Promise<void> {
    if (this.attemptId === null || this.completing()) {
      return;
    }
    this.completing.set(true);
    try {
      const attempt = await firstValueFrom(this.executionApi.continueBelowThreshold(this.attemptId));
      this.completion.update((result) => result === null ? null : ({ ...result, attempt }));
    } catch (error) {
      this.completionError.set(this.errorMessage(error));
    } finally {
      this.completing.set(false);
    }
  }

  async resetLab(lab: LabContent): Promise<void> {
    if (this.resetting()) return;
    const confirmed = globalThis.confirm(
      `Réinitialiser ${lab.code} ?\n\nLes tentatives, scores, exécutions, réponses, révisions et brouillons de ce laboratoire seront supprimés. Cette action est irréversible.`
    );
    if (!confirmed) return;

    this.resetting.set(true);
    this.resetNotice.set(null);
    this.completionError.set(null);
    try {
      await firstValueFrom(this.executionApi.resetLab(lab.code));
      if (this.draftTimer !== null) {
        clearTimeout(this.draftTimer);
        this.draftTimer = null;
      }
      await this.drafts.remove(lab.code);
      this.attemptId = null;
      this.code.set(lab.exercises[0]?.starterCode ?? '');
      this.sourceOrigin.set('EDITOR');
      this.execution.set(null);
      this.executionError.set(null);
      this.quizValues.set({});
      this.checklist.set(lab.checklist.map(() => false));
      this.completion.set(null);
      this.resumed.set(false);
      this.hintLevel.set(0);
      this.tutorResponse.set(null);
      this.tutorError.set(null);
      this.resetNotice.set(`${lab.code} a été remis à zéro. Une nouvelle tentative sera créée à la prochaine exécution.`);
    } catch (error) {
      this.completionError.set(this.errorMessage(error));
    } finally {
      this.resetting.set(false);
    }
  }

  async explainConcept(conceptCode: string): Promise<void> {
    await this.askTutor(() => firstValueFrom(this.tutorApi.explain(this.activeLabCode, conceptCode)));
  }

  async requestHint(): Promise<void> {
    const level = Math.min(3, this.hintLevel() + 1);
    this.hintLevel.set(level);
    await this.askTutor(() => firstValueFrom(this.tutorApi.hint(this.activeLabCode, this.code(), level)));
  }

  async reviewFreeText(questionCode: string): Promise<void> {
    const answer = this.quizValues()[questionCode];
    if (typeof answer !== 'string' || answer.trim().length === 0) {
      this.tutorError.set('Rédige d’abord une réponse avant de demander une correction.');
      return;
    }
    await this.askTutor(() => firstValueFrom(this.tutorApi.reviewAnswer(this.activeLabCode, questionCode, answer)));
  }

  private async askTutor(request: () => Promise<{ content: string }>): Promise<void> {
    if (this.tutorLoading()) return;
    this.tutorLoading.set(true);
    this.tutorError.set(null);
    try {
      this.tutorResponse.set((await request()).content);
      this.tutorAvailable.set(true);
    } catch (error) {
      this.tutorError.set(this.errorMessage(error));
      this.tutorAvailable.set(false);
    } finally {
      this.tutorLoading.set(false);
    }
  }

  private async ensureAttempt(): Promise<string> {
    if (this.attemptId === null) {
      const attempt = await firstValueFrom(this.executionApi.startAttempt(this.activeLabCode));
      this.attemptId = attempt.id;
    }
    return this.attemptId;
  }

  private scheduleDraft(): void {
    if (this.draftTimer !== null) clearTimeout(this.draftTimer);
    this.draftTimer = setTimeout(() => {
      this.draftTimer = null;
      void this.drafts.save(this.activeLabCode, this.code());
    }, 400);
  }

  private async restoreLocalDraft(labCode: string): Promise<void> {
    const sourceCode = await this.drafts.load(labCode);
    if (sourceCode !== null && this.activeLabCode === labCode && this.attemptId === null) {
      this.code.set(sourceCode);
      this.resumed.set(true);
    }
  }

  private errorMessage(error: unknown): string {
    if (error instanceof Error && !(error instanceof HttpErrorResponse)) {
      return error.message;
    }
    if (error instanceof HttpErrorResponse && typeof error.error?.detail === 'string') {
      return error.error.detail;
    }
    return "Impossible de terminer le laboratoire. Vérifie les réponses, l'exécution et la connexion à l'API.";
  }

  conceptsFor(codes: string[], concepts: KeyConcept[], sectionIndex: number): KeyConcept[] {
    if (codes.length === 0 && sectionIndex === 0) return concepts;
    return concepts.filter((concept) => codes.includes(concept.code));
  }

  unassignedConcepts(lab: LabContent): KeyConcept[] {
    const assigned = new Set(lab.sections.flatMap((section) => section.conceptCodes));
    if (assigned.size === 0 && lab.sections.length > 0) return [];
    return lab.keyConcepts.filter((concept) => !assigned.has(concept.code));
  }

  fileExtension(language: string): string {
    return ({ JAVA: '.java', PYTHON: '.py', TYPESCRIPT: '.ts' } as Record<string, string>)[language.toUpperCase()] ?? '.txt';
  }

  fileAccept(language: string): string {
    return ({ JAVA: '.java,text/x-java-source', PYTHON: '.py,text/x-python', TYPESCRIPT: '.ts,text/typescript' } as Record<string, string>)[language.toUpperCase()] ?? 'text/plain';
  }
}
