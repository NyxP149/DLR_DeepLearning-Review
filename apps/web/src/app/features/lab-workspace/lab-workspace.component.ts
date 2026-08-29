import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { catchError, firstValueFrom, map, Observable, of, startWith, switchMap, tap } from 'rxjs';

import {
  ExecutionApiService,
  ExecutionResult
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

  private attemptId: string | null = null;
  private activeLabCode = 'JAVA-01';

  readonly state$: Observable<LabViewState> = this.route.paramMap.pipe(
    map((params) => params.get('code') ?? 'JAVA-01'),
    switchMap((code) =>
      this.labApi.getLab(code).pipe(
        tap((lab) => {
          this.activeLabCode = lab.code;
          this.attemptId = null;
          this.execution.set(null);
          this.executionError.set(null);
          this.code.set(lab.exercises[0]?.starterCode ?? '');
        }),
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
  }

  async runCode(): Promise<void> {
    if (this.running() || this.code().trim().length === 0) {
      return;
    }

    this.running.set(true);
    this.execution.set(null);
    this.executionError.set(null);
    try {
      if (this.attemptId === null) {
        const attempt = await firstValueFrom(
          this.executionApi.startAttempt(this.activeLabCode)
        );
        this.attemptId = attempt.id;
      }
      const submission = await firstValueFrom(
        this.executionApi.submit(this.attemptId, this.code())
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
}
