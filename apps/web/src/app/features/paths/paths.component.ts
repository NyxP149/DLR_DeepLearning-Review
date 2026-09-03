import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, startWith } from 'rxjs';

import { LabApiService, LabProgressState, LabSummary, PathDescriptor, PathProgress } from '../../core/api/lab-api.service';

@Component({
  selector: 'dlr-paths',
  imports: [AsyncPipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Catalogue V3</p>
      <h1>Parcours professionnels</h1>
      <span>Neuf parcours exécutables, avec progression dédiée, projets portfolio et défis de synthèse.</span>
    </header>
    @if (state$ | async; as state) {
      @if (state.status === 'loading') {
        <p class="state">Chargement des parcours…</p>
      } @else if (state.status === 'error') {
        <p class="state error">L’API DLR est indisponible.</p>
      } @else {
        <section class="catalog" aria-label="Catalogue des parcours">
          @for (path of state.paths; track path.code) {
            <button type="button" class="path-card" [class.selected]="selectedPath() === path.code" [class.available]="path.status === 'AVAILABLE'" [class.beta]="path.status === 'BETA'" [disabled]="!isProgressivePath(path.code)" (click)="selectPath(path.code)">
              <div><span>{{ statusLabel(path.status) }}</span><small>{{ activityCountLabel(path.code, path.expectedActivityCount, state.labs) }}</small></div>
              <h2>{{ path.title }}</h2><p>{{ path.professionalObjectives[0] }}</p>
              <small>{{ path.prerequisites.length ? 'Après ' + path.prerequisites.join(', ') + ' · ' : '' }}{{ path.portfolioSkills.join(' · ') }}</small>
            </button>
          }
        </section>

        @if (selectedProgress(state.progresses); as progress) {
          <section class="path-progress" [class.java-progress]="selectedPath() === 'JAVA'" [attr.aria-label]="'Progression ' + selectedPath()">
            <div><p>Parcours actif</p><h2>{{ pathTitle(state.paths, selectedPath()) }}</h2><span>{{ progress.completedLabs }} / {{ progress.totalLabs }} étapes validées</span></div>
            <div class="progress-track" role="progressbar" [attr.aria-label]="'Progression du parcours ' + selectedPath()" [attr.aria-valuenow]="progress.progressPercent" aria-valuemin="0" aria-valuemax="100"><span [style.width.%]="progress.progressPercent"></span></div>
            <small>{{ progress.progressPercent }} % · Prochaine étape : {{ progress.nextLabCode ?? 'parcours terminé' }}</small>
          </section>
          <section class="path-grid" [attr.aria-label]="'Activités du parcours ' + selectedPath()">
            @for (lab of progress.labs; track lab.code; let index = $index) {
              @if (lab.state !== 'LOCKED') {
                <a class="lab-card" [class.done]="lab.state === 'COMPLETED'" [class.project]="lab.activityType === 'PROJECT'" [class.challenge]="lab.activityType === 'CHALLENGE'" [routerLink]="['/labs', lab.code]">
                  <span class="number">{{ activityMarker(lab.activityType, index) }}</span><div><p>{{ lab.code }} · {{ progressLabel(lab.state) }}</p><h2>{{ lab.title }}</h2><small>{{ lab.bestScore !== null ? 'Meilleur score ' + lab.bestScore + ' %' : (lab.prerequisites.length ? 'Après ' + lab.prerequisites.join(', ') : 'Point de départ') }}</small></div><span class="arrow">→</span>
                </a>
              } @else {
                <article class="lab-card locked" [class.project]="lab.activityType === 'PROJECT'" [class.challenge]="lab.activityType === 'CHALLENGE'"><span class="number">{{ activityMarker(lab.activityType, index) }}</span><div><p>{{ lab.code }} · VERROUILLÉ</p><h2>{{ lab.title }}</h2><small>Termine {{ lab.prerequisites.join(', ') }}</small></div><span class="arrow" aria-hidden="true">🔒</span></article>
              }
            }
          </section>
        }
      }
    }
  `,
  styles: [`
    :host{display:block}.page-header{margin-bottom:1.5rem}.page-header p{color:var(--accent);font-size:.78rem;font-weight:750;letter-spacing:.09em;text-transform:uppercase}.page-header h1{font-size:clamp(2rem,4vw,3rem);margin:.25rem 0}.page-header span,.lab-card p,.lab-card small,.path-card p,.path-card small{color:var(--text-muted)}
    .catalog{display:grid;gap:.7rem;grid-template-columns:repeat(4,1fr);margin-bottom:1.5rem}.path-card{background:var(--surface);border:1px solid var(--border);border-radius:.75rem;color:var(--text);cursor:pointer;font:inherit;opacity:.65;padding:.9rem;text-align:left}.path-card.available{border-color:#347865;opacity:1}.path-card.beta{border-color:#347865;opacity:1}.path-card.selected{border-color:var(--accent);box-shadow:inset 0 3px var(--accent)}.path-card:disabled{cursor:not-allowed}.path-card>div{align-items:center;display:flex;justify-content:space-between}.path-card>div span{color:var(--accent);font-size:.65rem;font-weight:800;text-transform:uppercase}.path-card h2{font-size:.95rem;margin:.8rem 0 .4rem}.path-card p{font-size:.72rem;line-height:1.45}
    .path-progress{background:linear-gradient(135deg,var(--surface),#132039);border:1px solid #347865;border-radius:.85rem;margin:1.7rem 0 .8rem;padding:1rem}.path-progress.java-progress{border-color:var(--accent)}.path-progress>div:first-child{align-items:end;display:grid;grid-template-columns:1fr auto}.path-progress p{color:var(--accent);font-size:.68rem;font-weight:800;margin:0;text-transform:uppercase}.path-progress h2{grid-column:1;margin:.15rem 0}.path-progress>div:first-child>span{color:var(--text-muted);font-size:.75rem;grid-column:2;grid-row:1/3}.progress-track{background:#0b1018;border-radius:99px;height:.55rem;margin:.8rem 0 .45rem;overflow:hidden}.progress-track span{background:linear-gradient(90deg,var(--success),var(--accent));display:block;height:100%}.path-progress small{color:var(--text-muted)}
    .path-grid{display:grid;gap:.7rem;grid-template-columns:repeat(6,minmax(0,1fr))}.lab-card{align-content:start;background:var(--surface);border:1px solid var(--border);border-radius:.75rem;color:var(--text);display:grid;gap:.7rem;min-height:9.5rem;padding:.85rem;text-decoration:none;transition:border-color .2s,transform .2s}.lab-card:hover,.lab-card:focus-visible{border-color:var(--accent);transform:translateY(-2px)}.lab-card.locked{filter:saturate(.35);opacity:.55}.lab-card.locked:hover{border-color:var(--border);transform:none}.lab-card.done{border-color:#347865}.lab-card.project{box-shadow:inset 0 3px #e7b85c}.lab-card.project .number{background:#3d3020;color:#ffd98b}.lab-card.challenge{box-shadow:inset 0 3px #e5688a}.lab-card.challenge .number{background:#422136;color:#ffb3c7}.number{align-items:center;background:var(--accent-soft);border-radius:.65rem;color:#b9b0ff;display:flex;font-size:1rem;font-weight:800;height:2.5rem;justify-content:center;width:2.5rem}.lab-card h2{font-size:1rem;margin:.2rem 0 .35rem}.lab-card p{font-size:.72rem;margin:0}.arrow{align-self:end;color:var(--success);font-size:1.2rem;justify-self:end}.state{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1rem}.error{color:var(--danger)}
    @media(max-width:1050px){.path-grid{grid-template-columns:repeat(3,1fr)}.catalog{grid-template-columns:repeat(3,1fr)}}@media(max-width:620px){.path-grid,.catalog{grid-template-columns:repeat(2,1fr)}.path-progress>div:first-child{display:block}.path-progress>div:first-child>span{display:block;margin-top:.35rem}}@media(prefers-reduced-motion:reduce){.lab-card{transition:none}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PathsComponent {
  private readonly labs = inject(LabApiService);
  readonly progressivePaths = ['JAVA', 'PYTHON', 'TYPESCRIPT', 'LEARN_LLM', 'SPRING_BOOT', 'ANGULAR', 'SQL', 'DEVOPS', 'ARCHITECTURE'] as const;
  readonly selectedPath = signal<string>('JAVA');
  private readonly progressRequests = Object.fromEntries(this.progressivePaths.map((code) => [code, this.labs.pathProgress(code)]));
  readonly state$ = forkJoin({ labs: this.labs.listLabs(), paths: this.labs.listPaths(), progresses: forkJoin(this.progressRequests) }).pipe(
    map(({ labs, paths, progresses }) => ({ status: 'loaded' as const, labs, paths, progresses })),
    startWith({ status: 'loading' as const, labs: [] as LabSummary[], paths: [] as PathDescriptor[], progresses: {} as Record<string, PathProgress> }),
    catchError(() => of({ status: 'error' as const, labs: [] as LabSummary[], paths: [] as PathDescriptor[], progresses: {} as Record<string, PathProgress> }))
  );
  selectPath(code: string): void { if (this.isProgressivePath(code)) this.selectedPath.set(code); }
  isProgressivePath(code: string): boolean { return this.progressivePaths.some((item) => item === code); }
  selectedProgress(progresses: Record<string, PathProgress>): PathProgress | undefined { return progresses[this.selectedPath()]; }
  pathTitle(paths: PathDescriptor[], code: string): string { return paths.find((path) => path.code === code)?.title ?? code; }
  progressLabel(state: LabProgressState): string { return ({ AVAILABLE: 'DISPONIBLE', IN_PROGRESS: 'EN COURS', ACTION_REQUIRED: 'DÉCISION REQUISE', COMPLETED: 'VALIDÉ', LOCKED: 'VERROUILLÉ' } as Record<LabProgressState, string>)[state]; }
  statusLabel(status: string): string { return ({ AVAILABLE: 'Disponible', BETA: 'Bêta exécutable', LOCKED: 'Verrouillé', PLANNED: 'Planifié' } as Record<string, string>)[status] ?? status; }
  activityMarker(type: 'LAB' | 'PROJECT' | 'CHALLENGE', index: number): string | number { return type === 'PROJECT' ? 'P' : type === 'CHALLENGE' ? 'D' : index + 1; }
  activityCountLabel(pathCode: string, expected: number, labs: LabSummary[]): string {
    const prefix = pathCode === 'LEARN_LLM' ? 'LLM-' : `${pathCode}-`;
    const available = labs.filter((lab) => lab.code.startsWith(prefix)).length;
    return `${available} disponible${available === 1 ? '' : 's'} / ${expected} prévue${expected === 1 ? '' : 's'}`;
  }
}
