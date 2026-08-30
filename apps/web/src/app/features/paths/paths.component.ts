import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, startWith } from 'rxjs';

import { LabApiService, LabProgressState, LabSummary } from '../../core/api/lab-api.service';

@Component({
  selector: 'dlr-paths',
  imports: [AsyncPipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Catalogue V2.6</p>
      <h1>Parcours professionnels</h1>
      <span>Python professionnel propose désormais six étapes progressives, jusqu’au premier projet portfolio.</span>
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
        <section class="python-progress" aria-label="Progression Python">
          <div><p>Parcours actif V2.6</p><h2>Python professionnel</h2><span>{{ state.python.completedLabs }} / {{ state.python.totalLabs }} étapes validées</span></div>
          <div class="progress-track" role="progressbar" aria-label="Progression du parcours Python" [attr.aria-valuenow]="state.python.progressPercent" aria-valuemin="0" aria-valuemax="100"><span [style.width.%]="state.python.progressPercent"></span></div>
          <small>{{ state.python.progressPercent }} % · Prochaine étape : {{ state.python.nextLabCode ?? 'parcours terminé' }}</small>
        </section>
        <section class="path-grid python-grid" aria-label="Laboratoires Python professionnel">
          @for (lab of state.python.labs; track lab.code; let index = $index) {
            @if (lab.state !== 'LOCKED') {
              <a class="lab-card" [class.done]="lab.state === 'COMPLETED'" [class.project]="lab.activityType === 'PROJECT'" [routerLink]="['/labs', lab.code]">
                <span class="number">{{ lab.activityType === 'PROJECT' ? 'P' : index + 1 }}</span><div><p>{{ lab.code }} · {{ progressLabel(lab.state) }}</p><h2>{{ lab.title }}</h2><small>{{ lab.bestScore !== null ? 'Meilleur score ' + lab.bestScore + ' %' : (lab.prerequisites.length ? 'Après ' + lab.prerequisites.join(', ') : 'Point de départ') }}</small></div><span class="arrow">→</span>
              </a>
            } @else {
              <article class="lab-card locked"><span class="number">{{ lab.activityType === 'PROJECT' ? 'P' : index + 1 }}</span><div><p>{{ lab.code }} · VERROUILLÉ</p><h2>{{ lab.title }}</h2><small>Termine {{ lab.prerequisites.join(', ') }}</small></div><span class="arrow" aria-hidden="true">🔒</span></article>
            }
          }
        </section>
        <div class="lab-title"><div><p>Tranches suivantes</p><h2>TypeScript et Learn LLMs</h2></div><span>2 laboratoires exécutables</span></div>
        <section class="path-grid beta-labs" aria-label="Laboratoires TypeScript et Learn LLMs">
          @for (lab of futureLabs(state.labs); track lab.code) {
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
    .catalog{display:grid;gap:.7rem;grid-template-columns:repeat(4,1fr);margin-bottom:1.5rem}.catalog article{background:var(--surface);border:1px solid var(--border);border-radius:.75rem;opacity:.65;padding:.9rem}.catalog article.available{border-color:var(--accent);box-shadow:inset 0 3px var(--accent);opacity:1}.catalog article.beta{border-color:#347865;opacity:1}.catalog article.beta>div span{color:var(--success)}.catalog article>div,.lab-title{align-items:center;display:flex;justify-content:space-between}.catalog article>div span{color:var(--accent);font-size:.65rem;font-weight:800;text-transform:uppercase}.catalog h2{font-size:.95rem;margin:.8rem 0 .4rem}.catalog p{font-size:.72rem;line-height:1.45}.lab-title{margin:1.5rem 0 .7rem}.lab-title p,.python-progress p{color:var(--accent);font-size:.68rem;font-weight:800;margin:0;text-transform:uppercase}.lab-title h2,.python-progress h2{margin:.15rem 0}.lab-title>span{color:var(--text-muted);font-size:.75rem}
    .python-progress{background:linear-gradient(135deg,var(--surface),#132039);border:1px solid #347865;border-radius:.85rem;margin:1.7rem 0 .8rem;padding:1rem}.python-progress>div:first-child{align-items:end;display:grid;grid-template-columns:1fr auto}.python-progress h2{grid-column:1}.python-progress>div:first-child>span{color:var(--text-muted);font-size:.75rem;grid-column:2;grid-row:1/3}.progress-track{background:#0b1018;border-radius:99px;height:.55rem;margin:.8rem 0 .45rem;overflow:hidden}.progress-track span{background:linear-gradient(90deg,var(--success),var(--accent));display:block;height:100%}.python-progress small{color:var(--text-muted)}
    .path-grid { display: grid; gap: .7rem; grid-template-columns: repeat(6,minmax(0,1fr)); }
    .path-grid.beta-labs{grid-template-columns:repeat(2,minmax(0,1fr));max-width:65%}
    .lab-card { align-content: start; background: var(--surface); border: 1px solid var(--border); border-radius: .75rem; color: var(--text); display: grid; gap: .7rem; min-height: 9.5rem; padding: .85rem; text-decoration: none; transition: border-color .2s, transform .2s; }
    .lab-card:hover, .lab-card:focus-visible { border-color: var(--accent); transform: translateY(-2px); }
    .lab-card.locked{filter:saturate(.35);opacity:.55}.lab-card.locked:hover{border-color:var(--border);transform:none}.lab-card.done{border-color:#347865}.lab-card.project{box-shadow:inset 0 3px #e7b85c}.lab-card.project .number{background:#3d3020;color:#ffd98b}
    .number { align-items: center; background: var(--accent-soft); border-radius: .65rem; color: #b9b0ff; display: flex; font-size: 1rem; font-weight: 800; height: 2.5rem; justify-content: center; width: 2.5rem; }
    .lab-card h2 { font-size: 1rem; margin: .2rem 0 .35rem; }
    .lab-card p { font-size: .72rem; margin: 0; }
    .arrow { align-self:end;color:var(--success);font-size:1.2rem;justify-self:end; }
    .state { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 1rem; }
    .error { color: var(--danger); }
    @media(max-width:1050px){.path-grid{grid-template-columns:repeat(3,1fr)}.catalog{grid-template-columns:repeat(3,1fr)}.path-grid.beta-labs{max-width:none}}@media(max-width:620px){.path-grid,.catalog,.path-grid.beta-labs{grid-template-columns:repeat(2,1fr)}.python-progress>div:first-child{display:block}.python-progress>div:first-child>span{display:block;margin-top:.35rem}}
    @media (prefers-reduced-motion: reduce) { .lab-card { transition: none; } }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PathsComponent {
  private readonly labs = inject(LabApiService);
  readonly state$ = forkJoin({ labs: this.labs.listLabs(), paths: this.labs.listPaths(), python: this.labs.pathProgress('PYTHON') }).pipe(
    map(({ labs, paths, python }) => ({ status: 'loaded' as const, labs, paths, python })),
    startWith({ status: 'loading' as const, labs: [], paths: [], python: { pathCode: 'PYTHON', totalLabs: 0, completedLabs: 0, progressPercent: 0, nextLabCode: null, labs: [] } }),
    catchError(() => of({ status: 'error' as const, labs: [], paths: [], python: { pathCode: 'PYTHON', totalLabs: 0, completedLabs: 0, progressPercent: 0, nextLabCode: null, labs: [] } }))
  );
  javaLabs(labs: LabSummary[]): LabSummary[] { return labs.filter((lab) => lab.code.startsWith('JAVA-')); }
  futureLabs(labs: LabSummary[]): LabSummary[] {
    const order = ['TYPESCRIPT-01', 'LLM-01'];
    return labs
      .filter((lab) => order.includes(lab.code))
      .sort((left, right) => order.indexOf(left.code) - order.indexOf(right.code));
  }
  progressLabel(state: LabProgressState): string { return ({ AVAILABLE: 'DISPONIBLE', IN_PROGRESS: 'EN COURS', ACTION_REQUIRED: 'DÉCISION REQUISE', COMPLETED: 'VALIDÉ', LOCKED: 'VERROUILLÉ' } as Record<LabProgressState, string>)[state]; }
  statusLabel(status: string): string { return ({ AVAILABLE: 'Disponible', BETA: 'Bêta exécutable', LOCKED: 'Verrouillé', PLANNED: 'Planifié' } as Record<string, string>)[status] ?? status; }
}
