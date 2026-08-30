import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, startWith } from 'rxjs';

import { LabApiService } from '../../core/api/lab-api.service';

@Component({
  selector: 'dlr-paths',
  imports: [AsyncPipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Catalogue V2</p>
      <h1>Parcours professionnels</h1>
      <span>Java est disponible ; les prochains modules réutilisent le même moteur pédagogique.</span>
    </header>
    @if (state$ | async; as state) {
      @if (state.status === 'loading') {
        <p class="state">Chargement du parcours…</p>
      } @else if (state.status === 'error') {
        <p class="state error">L’API DLR est indisponible.</p>
      } @else {
        <section class="catalog" aria-label="Catalogue des parcours">
          @for (path of state.paths; track path.code) {
            <article [class.available]="path.status === 'AVAILABLE'"><div><span>{{ path.status === 'AVAILABLE' ? 'Disponible' : 'Planifié' }}</span><small>{{ path.expectedActivityCount }} activités</small></div><h2>{{ path.title }}</h2><p>{{ path.professionalObjectives[0] }}</p><small>{{ path.portfolioSkills.join(' · ') }}</small></article>
          }
        </section>
        <div class="lab-title"><div><p>Parcours actif</p><h2>Fondamentaux Java</h2></div><span>6 laboratoires</span></div>
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
    .page-header span, .lab-card p, .lab-card small,.catalog p,.catalog small { color: var(--text-muted); }
    .catalog{display:grid;gap:.7rem;grid-template-columns:repeat(5,1fr);margin-bottom:1.5rem}.catalog article{background:var(--surface);border:1px solid var(--border);border-radius:.75rem;opacity:.65;padding:.9rem}.catalog article.available{border-color:var(--accent);box-shadow:inset 0 3px var(--accent);opacity:1}.catalog article>div,.lab-title{align-items:center;display:flex;justify-content:space-between}.catalog article>div span{color:var(--accent);font-size:.65rem;font-weight:800;text-transform:uppercase}.catalog h2{font-size:.95rem;margin:.8rem 0 .4rem}.catalog p{font-size:.72rem;line-height:1.45}.lab-title{margin-bottom:.7rem}.lab-title p{color:var(--accent);font-size:.68rem;font-weight:800;margin:0;text-transform:uppercase}.lab-title h2{margin:.15rem 0}.lab-title>span{color:var(--text-muted);font-size:.75rem}
    .path-grid { display: grid; gap: .7rem; grid-template-columns: repeat(6,minmax(0,1fr)); }
    .lab-card { align-content: start; background: var(--surface); border: 1px solid var(--border); border-radius: .75rem; color: var(--text); display: grid; gap: .7rem; min-height: 9.5rem; padding: .85rem; text-decoration: none; transition: border-color .2s, transform .2s; }
    .lab-card:hover, .lab-card:focus-visible { border-color: var(--accent); transform: translateY(-2px); }
    .number { align-items: center; background: var(--accent-soft); border-radius: .65rem; color: #b9b0ff; display: flex; font-size: 1rem; font-weight: 800; height: 2.5rem; justify-content: center; width: 2.5rem; }
    .lab-card h2 { font-size: 1rem; margin: .2rem 0 .35rem; }
    .lab-card p { font-size: .72rem; margin: 0; }
    .arrow { align-self:end;color:var(--success);font-size:1.2rem;justify-self:end; }
    .state { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 1rem; }
    .error { color: var(--danger); }
    @media(max-width:1050px){.path-grid{grid-template-columns:repeat(3,1fr)}.catalog{grid-template-columns:repeat(3,1fr)}}@media(max-width:620px){.path-grid,.catalog{grid-template-columns:repeat(2,1fr)}}
    @media (prefers-reduced-motion: reduce) { .lab-card { transition: none; } }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PathsComponent {
  private readonly labs = inject(LabApiService);
  readonly state$ = forkJoin({ labs: this.labs.listLabs(), paths: this.labs.listPaths() }).pipe(
    map(({ labs, paths }) => ({ status: 'loaded' as const, labs, paths })),
    startWith({ status: 'loading' as const, labs: [], paths: [] }),
    catchError(() => of({ status: 'error' as const, labs: [], paths: [] }))
  );
}
