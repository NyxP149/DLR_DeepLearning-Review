import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, startWith } from 'rxjs';

import { LabApiService, LabSummary } from '../../core/api/lab-api.service';

@Component({
  selector: 'dlr-paths',
  imports: [AsyncPipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Catalogue V2</p>
      <h1>Parcours professionnels</h1>
      <span>Java est stable ; Python, TypeScript et Learn LLMs inaugurent les parcours exécutables V2.5.</span>
    </header>
    @if (state$ | async; as state) {
      @if (state.status === 'loading') {
        <p class="state">Chargement du parcours…</p>
      } @else if (state.status === 'error') {
        <p class="state error">L’API DLR est indisponible.</p>
      } @else {
        <section class="catalog" aria-label="Catalogue des parcours">
          @for (path of state.paths; track path.code) {
            <article [class.available]="path.status === 'AVAILABLE'" [class.beta]="path.status === 'BETA'"><div><span>{{ statusLabel(path.status) }}</span><small>{{ path.expectedActivityCount }} activités prévues</small></div><h2>{{ path.title }}</h2><p>{{ path.professionalObjectives[0] }}</p><small>{{ path.prerequisites.length ? 'Après ' + path.prerequisites.join(', ') + ' · ' : '' }}{{ path.portfolioSkills.join(' · ') }}</small></article>
          }
        </section>
        <div class="lab-title"><div><p>Parcours actif</p><h2>Fondamentaux Java</h2></div><span>6 laboratoires</span></div>
        <section class="path-grid" aria-label="Laboratoires Java">
          @for (lab of javaLabs(state.labs); track lab.code) {
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
        <div class="lab-title"><div><p>Nouvelles tranches V2.5</p><h2>Python, TypeScript et Learn LLMs</h2></div><span>3 laboratoires exécutables</span></div>
        <section class="path-grid beta-labs" aria-label="Nouveaux laboratoires V2.5">
          @for (lab of v25Labs(state.labs); track lab.code) {
            <a class="lab-card" [routerLink]="['/labs', lab.code]"><span class="number">{{ lab.code === 'TYPESCRIPT-01' ? 'TS' : lab.code === 'LLM-01' ? 'AI' : 'Py' }}</span><div><p>{{ lab.code }} · BÊTA</p><h2>{{ lab.title }}</h2><small>Runner {{ lab.language }} isolé</small></div><span class="arrow">→</span></a>
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
    .catalog{display:grid;gap:.7rem;grid-template-columns:repeat(4,1fr);margin-bottom:1.5rem}.catalog article{background:var(--surface);border:1px solid var(--border);border-radius:.75rem;opacity:.65;padding:.9rem}.catalog article.available{border-color:var(--accent);box-shadow:inset 0 3px var(--accent);opacity:1}.catalog article.beta{border-color:#347865;opacity:1}.catalog article.beta>div span{color:var(--success)}.catalog article>div,.lab-title{align-items:center;display:flex;justify-content:space-between}.catalog article>div span{color:var(--accent);font-size:.65rem;font-weight:800;text-transform:uppercase}.catalog h2{font-size:.95rem;margin:.8rem 0 .4rem}.catalog p{font-size:.72rem;line-height:1.45}.lab-title{margin:1.5rem 0 .7rem}.lab-title p{color:var(--accent);font-size:.68rem;font-weight:800;margin:0;text-transform:uppercase}.lab-title h2{margin:.15rem 0}.lab-title>span{color:var(--text-muted);font-size:.75rem}.beta-labs{grid-template-columns:repeat(3,1fr)}
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
  javaLabs(labs: LabSummary[]): LabSummary[] { return labs.filter((lab) => lab.code.startsWith('JAVA-')); }
  v25Labs(labs: LabSummary[]): LabSummary[] {
    const order = ['PYTHON-01', 'TYPESCRIPT-01', 'LLM-01'];
    return labs
      .filter((lab) => !lab.code.startsWith('JAVA-'))
      .sort((left, right) => order.indexOf(left.code) - order.indexOf(right.code));
  }
  statusLabel(status: string): string { return ({ AVAILABLE: 'Disponible', BETA: 'Bêta exécutable', LOCKED: 'Verrouillé', PLANNED: 'Planifié' } as Record<string, string>)[status] ?? status; }
}
