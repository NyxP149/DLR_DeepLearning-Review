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
import { LabContent } from './lab.model';

type LabViewState =
  | { status: 'loading' }
  | { status: 'loaded'; lab: LabContent }
  | { status: 'error'; message: string };

@Component({
  selector: 'dlr-lab-workspace',
  imports: [AsyncPipe],
  templateUrl: './lab-workspace.component.html',
  styleUrl: './lab-workspace.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LabWorkspaceComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly labApi = inject(LabApiService);
  private readonly executionApi = inject(ExecutionApiService);

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

  private attemptId: string | null = null;
  private activeLabCode = 'JAVA-01';

  readonly state$: Observable<LabViewState> = this.route.paramMap.pipe(
    map((params) => params.get('code') ?? 'JAVA-01'),
    switchMap((code) =>
      this.labApi.getLab(code).pipe(
        switchMap((lab) => this.executionApi.currentWorkspace(lab.code).pipe(
          tap((workspace) => {
          this.activeLabCode = lab.code;
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
          this.code.set(workspace?.sourceCode ?? lab.exercises[0]?.starterCode ?? '');
          this.sourceOrigin.set(workspace?.sourceOrigin ?? 'EDITOR');
          this.resumed.set(workspace !== null);
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

  onCodeInput(event: Event): void {
    this.code.set((event.target as HTMLTextAreaElement).value);
    this.sourceOrigin.set('EDITOR');
  }

  async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;
    if (!file.name.toLowerCase().endsWith('.java')) {
      this.executionError.set('Seuls les fichiers source .java sont acceptés.');
      return;
    }
    if (file.size > 65_536) {
      this.executionError.set('Le fichier dépasse la limite de 64 Kio.');
      return;
    }
    this.code.set(await file.text());
    this.sourceOrigin.set('IMPORT');
    this.executionError.set(null);
  }

  async runCode(): Promise<void> {
    if (this.running() || this.code().trim().length === 0) {
      return;
    }

    this.running.set(true);
    this.execution.set(null);
    this.executionError.set(null);
    try {
      const attemptId = await this.ensureAttempt();
      const submission = await firstValueFrom(
        this.executionApi.submit(attemptId, this.code(), this.sourceOrigin())
      );
      const result = await firstValueFrom(this.executionApi.run(submission.id));
      this.execution.set(result);
    } catch {
      this.executionError.set(
        "L'exécution a échoué. Vérifie l'API, PostgreSQL, Docker Desktop et l'image du runner Java."
      );
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

  private async ensureAttempt(): Promise<string> {
    if (this.attemptId === null) {
      const attempt = await firstValueFrom(this.executionApi.startAttempt(this.activeLabCode));
      this.attemptId = attempt.id;
    }
    return this.attemptId;
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
}
