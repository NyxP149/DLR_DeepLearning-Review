import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { catchError, map, Observable, of, startWith, switchMap } from 'rxjs';

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

  readonly state$: Observable<LabViewState> = this.route.paramMap.pipe(
    map((params) => params.get('code') ?? 'JAVA-01'),
    switchMap((code) =>
      this.labApi.getLab(code).pipe(
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
}

