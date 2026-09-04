import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnDestroy, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RouterLink } from '@angular/router';
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
import { LabMemoryStoreService } from '../../core/storage/lab-memory-store.service';
import { NotesApiService } from '../../core/api/notes-api.service';
import { KeyConcept, LabContent } from './lab.model';
import { CodeEditorComponent } from '../../shared/code-editor/code-editor.component';
import { EXECUTION_AVAILABLE } from '../../core/api/api-config';

type LabViewState =
  | { status: 'loading' }
  | { status: 'loaded'; lab: LabContent }
  | { status: 'error'; message: string };

@Component({
  selector: 'dlr-lab-workspace',
  imports: [AsyncPipe, CodeEditorComponent, RouterLink],
  templateUrl: './lab-workspace.component.html',
  styleUrl: './lab-workspace.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LabWorkspaceComponent implements OnDestroy {
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
  private readonly memory = inject(LabMemoryStoreService);
  private readonly notesApi = inject(NotesApiService);

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
  readonly personalNote = signal('');
  readonly noteSaving = signal(false);
  readonly noteStatus = signal('Prête à écrire');
  readonly reflectionStatus = signal('Les réponses sont sauvegardées pendant la saisie.');
  readonly reflectionAnalyses = signal<Record<string, string>>({});
  readonly deletingAnalysis = signal<string | null>(null);

  private attemptId: string | null = null;
  private attemptPromise: Promise<string> | null = null;
  private activeLabCode = 'JAVA-01';
  private activeLanguage = 'JAVA';
  private draftTimer: ReturnType<typeof setTimeout> | null = null;
  private noteTimer: ReturnType<typeof setTimeout> | null = null;
  private readonly reflectionTimers = new Map<string, ReturnType<typeof setTimeout>>();

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
          this.clearReflectionTimers();
          this.activeLabCode = lab.code;
          this.activeLanguage = lab.language;
          this.attemptId = workspace?.attempt.id ?? null;
          this.attemptPromise = null;
          this.execution.set(null);
          this.executionError.set(null);
          const serverAnswers = Object.fromEntries((workspace?.quizAnswers ?? []).map((answer) => [
            answer.questionId,
            answer.selectedChoice ?? answer.answerText ?? ''
          ]));
          this.quizValues.set({ ...serverAnswers, ...this.memory.loadReflections(lab.code) });
          this.checklist.set(workspace?.checklist.length === lab.checklist.length
            ? workspace.checklist
            : lab.checklist.map(() => false));
          this.completion.set(null);
          this.completionError.set(null);
          this.resetNotice.set(null);
          this.code.set(workspace?.sourceCode ?? lab.exercises[0]?.starterCode ?? '');
          this.sourceOrigin.set(workspace?.sourceOrigin ?? 'EDITOR');
          this.resumed.set(workspace !== null);
          this.loadPersonalNote(lab.code);
          this.loadReflectionAnalyses(lab.code);
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
    this.scheduleReflection(questionId, choice);
  }

  onQuizText(questionId: string, event: Event): void {
    const value = (event.target as HTMLTextAreaElement).value;
    this.quizValues.update((values) => ({ ...values, [questionId]: value }));
    this.scheduleReflection(questionId, value);
  }

  onPersonalNoteChange(event: Event): void {
    const content = (event.target as HTMLTextAreaElement).value;
    const labCode = this.activeLabCode;
    this.personalNote.set(content);
    this.memory.saveNote(labCode, content);
    this.noteStatus.set('Sauvegardée sur cet appareil');
    if (this.noteTimer !== null) clearTimeout(this.noteTimer);
    this.noteSaving.set(true);
    this.noteTimer = setTimeout(() => this.persistPersonalNote(labCode, content), 650);
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
      `Réinitialiser ${lab.code} ?\n\nLes tentatives, scores, exécutions, réponses, révisions et brouillons de ce laboratoire seront supprimés. Tes notes personnelles et les analyses Ollama seront conservées. Cette action est irréversible.`
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
      this.memory.removeReflections(lab.code);
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
    if (this.tutorLoading()) return;
    const labCode = this.activeLabCode;
    this.tutorLoading.set(true);
    this.tutorError.set(null);
    try {
      const response = await firstValueFrom(this.tutorApi.reviewAnswer(labCode, questionCode, answer));
      this.tutorResponse.set(response.content);
      this.tutorAvailable.set(true);
      this.reflectionAnalyses.update((analyses) => ({ ...analyses, [questionCode]: response.content }));
      this.memory.saveAnalysis(labCode, questionCode, response.content);
      try {
        await firstValueFrom(this.notesApi.saveAnalysis(labCode, questionCode, response.content));
      } catch {
        this.tutorError.set("L'analyse reste conservée sur cet appareil ; sa synchronisation avec Neon est en attente.");
      }
    } catch (error) {
      this.tutorError.set(this.errorMessage(error));
      this.tutorAvailable.set(false);
    } finally {
      this.tutorLoading.set(false);
    }
  }

  async deleteReflectionAnalysis(questionCode: string): Promise<void> {
    const labCode = this.activeLabCode;
    this.deletingAnalysis.set(questionCode);
    this.memory.deleteAnalysis(labCode, questionCode);
    this.reflectionAnalyses.update((analyses) => {
      const updated = { ...analyses };
      delete updated[questionCode];
      return updated;
    });
    try {
      await firstValueFrom(this.notesApi.deleteAnalysis(labCode, questionCode));
    } catch {
      this.tutorError.set("L'analyse est effacée sur cet appareil, mais sa suppression dans Neon devra être réessayée.");
    } finally {
      this.deletingAnalysis.set(null);
    }
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
    if (this.attemptId !== null) return this.attemptId;
    if (this.attemptPromise === null) {
      this.attemptPromise = firstValueFrom(this.executionApi.startAttempt(this.activeLabCode))
        .then((attempt) => {
          this.attemptId = attempt.id;
          return attempt.id;
        })
        .finally(() => { this.attemptPromise = null; });
    }
    return this.attemptPromise;
  }

  private scheduleDraft(): void {
    if (this.draftTimer !== null) clearTimeout(this.draftTimer);
    this.draftTimer = setTimeout(() => {
      this.draftTimer = null;
      void this.drafts.save(this.activeLabCode, this.code());
    }, 400);
  }

  private scheduleReflection(questionId: string, value: number | string): void {
    const labCode = this.activeLabCode;
    this.memory.saveReflection(labCode, questionId, value);
    const previous = this.reflectionTimers.get(questionId);
    if (previous !== undefined) clearTimeout(previous);
    this.reflectionStatus.set('Sauvegarde de la réponse…');
    this.reflectionTimers.set(questionId, setTimeout(async () => {
      this.reflectionTimers.delete(questionId);
      try {
        if (typeof value === 'string' && value.trim().length === 0) {
          if (this.attemptId !== null) {
            await firstValueFrom(this.executionApi.deleteQuizAnswer(this.attemptId, questionId));
          }
        } else {
          const attemptId = await this.ensureAttempt();
          await firstValueFrom(this.executionApi.answerQuiz(
            attemptId,
            questionId,
            typeof value === 'number' ? { selectedChoice: value } : { answerText: value }
          ));
        }
        if (this.activeLabCode === labCode) this.reflectionStatus.set('Réponse sauvegardée dans Neon et sur cet appareil.');
      } catch {
        if (this.activeLabCode === labCode) this.reflectionStatus.set('Réponse conservée sur cet appareil · synchronisation en attente.');
      }
    }, 700));
  }

  private loadPersonalNote(labCode: string): void {
    const localNote = this.memory.loadNote(labCode);
    const localUpdatedAt = this.memory.noteUpdatedAt(labCode);
    this.personalNote.set(localNote);
    this.noteStatus.set(localNote ? 'Note locale restaurée' : 'Prête à écrire');
    this.notesApi.get(labCode).subscribe({
      next: (note) => {
        if (this.activeLabCode !== labCode) return;
        const localIsNewer = Boolean(localNote.trim() && localUpdatedAt
          && (!note.updatedAt || new Date(localUpdatedAt).getTime() > new Date(note.updatedAt).getTime()));
        if (localIsNewer) {
          this.persistPersonalNote(labCode, localNote);
        } else if (note.content) {
          this.personalNote.set(note.content);
          this.memory.saveNote(labCode, note.content, note.updatedAt ?? undefined);
          this.noteStatus.set('Synchronisée avec Neon');
        } else if (localNote.trim()) {
          this.persistPersonalNote(labCode, localNote);
        }
      },
      error: () => {
        if (this.activeLabCode === labCode) this.noteStatus.set('Note locale · synchronisation en attente');
      }
    });
  }

  private loadReflectionAnalyses(labCode: string): void {
    const localAnalyses = this.memory.loadAnalyses(labCode);
    this.reflectionAnalyses.set(localAnalyses);
    this.notesApi.analyses(labCode).subscribe({
      next: (analyses) => {
        if (this.activeLabCode !== labCode) return;
        const serverAnalyses = Object.fromEntries(analyses.map((analysis) => [analysis.questionId, analysis.content]));
        const merged = { ...localAnalyses, ...serverAnalyses };
        this.reflectionAnalyses.set(merged);
        for (const [questionId, content] of Object.entries(merged)) {
          this.memory.saveAnalysis(labCode, questionId, content);
        }
      }
    });
  }

  private persistPersonalNote(labCode: string, content: string): void {
    this.noteTimer = null;
    this.notesApi.save(labCode, content).subscribe({
      next: () => {
        if (this.activeLabCode === labCode && this.personalNote() === content) {
          this.noteSaving.set(false);
          this.noteStatus.set('Sauvegardée dans Neon et sur cet appareil');
        }
      },
      error: () => {
        if (this.activeLabCode === labCode && this.personalNote() === content) {
          this.noteSaving.set(false);
          this.noteStatus.set('Sauvegardée sur cet appareil · synchronisation en attente');
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.draftTimer !== null) clearTimeout(this.draftTimer);
    if (this.noteTimer !== null) clearTimeout(this.noteTimer);
    this.clearReflectionTimers();
  }

  private clearReflectionTimers(): void {
    for (const timer of this.reflectionTimers.values()) clearTimeout(timer);
    this.reflectionTimers.clear();
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
