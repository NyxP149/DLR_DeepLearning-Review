import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, map, of, startWith } from 'rxjs';

import { LabApiService } from '../../core/api/lab-api.service';

@Component({
  selector: 'dlr-paths',
  imports: [AsyncPipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Parcours V1</p>
      <h1>Fondamentaux Java</h1>
      <span>Six laboratoires progressifs pour construire des bases solides.</span>
    </header>
    @if (state$ | async; as state) {
      @if (state.status === 'loading') {
        <p class="state">Chargement du parcours…</p>
      } @else if (state.status === 'error') {
        <p class="state error">L’API DLR est indisponible.</p>
      } @else {
        <section class="path-grid" aria-label="Laboratoires Java">
          @for (lab of state.labs; track lab.code) {
            <a class="lab-card" [routerLink]="['/labs', lab.code]">
              <span class="number">{{ lab.number }}</span>
              <div>
                <p>{{ lab.code }} · {{ lab.difficulty }}</p>
                <h2>{{ lab.title }}</h2>
                <small>Seuil recommandé {{ lab.threshold }} %</small>
              </div>
              <span class="arrow" aria-hidden="true">→</span>
            </a>
          }
        </section>
      }
    }
  `,
  styles: [`
    :host { display: block; }
    .page-header { margin-bottom: 1.5rem; }
    .page-header p { color: var(--accent); font-size: .78rem; font-weight: 750; letter-spacing: .09em; text-transform: uppercase; }
    .page-header h1 { font-size: clamp(2rem, 4vw, 3rem); margin: .25rem 0; }
    .page-header span, .lab-card p, .lab-card small { color: var(--text-muted); }
    .path-grid { display: grid; gap: .85rem; }
    .lab-card { align-items: center; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); color: var(--text); display: grid; gap: 1rem; grid-template-columns: auto 1fr auto; padding: 1rem; text-decoration: none; transition: border-color .2s, transform .2s; }
    .lab-card:hover, .lab-card:focus-visible { border-color: var(--accent); transform: translateY(-2px); }
    .number { align-items: center; background: var(--accent-soft); border-radius: .8rem; color: #a9c8ff; display: flex; font-size: 1.1rem; font-weight: 800; height: 3rem; justify-content: center; width: 3rem; }
    .lab-card h2 { font-size: 1rem; margin: .2rem 0 .35rem; }
    .lab-card p { font-size: .72rem; margin: 0; }
    .arrow { color: var(--accent); font-size: 1.4rem; }
    .state { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 1rem; }
    .error { color: var(--danger); }
    @media (prefers-reduced-motion: reduce) { .lab-card { transition: none; } }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PathsComponent {
  private readonly labs = inject(LabApiService);
  readonly state$ = this.labs.listLabs().pipe(
    map((labs) => ({ status: 'loaded' as const, labs })),
    startWith({ status: 'loading' as const, labs: [] }),
    catchError(() => of({ status: 'error' as const, labs: [] }))
  );
}
