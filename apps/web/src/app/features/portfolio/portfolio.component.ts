import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';

import { PortfolioApiService, PortfolioProject } from '../../core/api/portfolio-api.service';

@Component({
  selector: 'dlr-portfolio',
  template: `
    <header class="page-header"><p>Portfolio V2</p><h1>Transforme tes preuves en projet</h1><span>Privé par défaut. Rien n’est publié automatiquement.</span></header>
    <div class="layout">
      <section class="builder">
        <h2>Nouvelle fiche projet</h2>
        <label>Titre<input [value]="title()" (input)="title.set(value($event))" maxlength="160"></label>
        <label>Résumé public<textarea [value]="summary()" (input)="summary.set(value($event))" rows="4" maxlength="1200"></textarea></label>
        <fieldset><legend>Preuves à inclure</legend><div class="labs">
          @for (lab of labs; track lab) { <label><input type="checkbox" [checked]="selectedLabs().includes(lab)" (change)="toggleLab(lab)">{{ lab }}</label> }
        </div></fieldset>
        <label>Décisions techniques <small>une par ligne</small><textarea [value]="decisions()" (input)="decisions.set(value($event))" rows="4" maxlength="4000"></textarea></label>
        <button class="primary" type="button" (click)="create()" [disabled]="saving() || !title().trim() || !summary().trim() || selectedLabs().length === 0">{{ saving() ? 'Création…' : 'Créer le brouillon privé' }}</button>
        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
        <aside><strong>Contrôle de confidentialité</strong><span>Les scores, le profil, les conversations IA et la synchronisation ne sont jamais lus. Un secret potentiel bloque la création.</span></aside>
      </section>
      <section class="projects">
        <div class="section-title"><h2>Projets privés</h2><span>{{ projects().length }}</span></div>
        @if (projects().length === 0) { <p class="empty">Aucun projet. Crée une première sélection de preuves.</p> }
        @for (project of projects(); track project.id) {
          <article [class.active]="activeId() === project.id">
            <div><small>{{ project.status }}</small><h3>{{ project.title }}</h3><p>{{ project.summary }}</p><span>{{ project.labCodes.join(' · ') }}</span></div>
            <div class="actions"><button type="button" (click)="preview(project)">Aperçu README</button><button type="button" (click)="download(project)">Télécharger ZIP</button></div>
          </article>
        }
      </section>
    </div>
    @if (markdown()) { <section class="preview"><div><h2>Aperçu Markdown</h2><button type="button" (click)="markdown.set('')">Fermer</button></div><pre>{{ markdown() }}</pre></section> }
  `,
  styles: [`
    :host{display:block}.page-header{margin-bottom:1.4rem}.page-header p{color:var(--accent);font-size:.75rem;font-weight:800;letter-spacing:.09em;margin:0;text-transform:uppercase}.page-header h1{font-size:clamp(2rem,4vw,3rem);margin:.25rem 0}.page-header span,.builder small,.projects p,.projects article span,.empty{color:var(--text-muted)}.layout{display:grid;gap:1rem;grid-template-columns:minmax(300px,.8fr) minmax(400px,1.2fr)}.builder,.projects,.preview{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1.2rem}.builder h2,.projects h2,.preview h2{font-size:1rem;margin:0 0 1rem}.builder>label{color:var(--text-muted);display:grid;font-size:.75rem;font-weight:750;gap:.35rem;margin:.8rem 0}.builder input,.builder textarea{background:#0b1018;border:1px solid var(--border);border-radius:.55rem;color:var(--text);font:inherit;padding:.7rem;resize:vertical}fieldset{border:1px solid var(--border);border-radius:.6rem;margin:.8rem 0;padding:.8rem}legend{color:var(--text-muted);font-size:.75rem;font-weight:750}.labs{display:grid;gap:.5rem;grid-template-columns:repeat(3,1fr)}.labs label{align-items:center;display:flex;font-size:.78rem;gap:.35rem}.primary{background:var(--accent);border:0;border-radius:.55rem;color:#07111f;cursor:pointer;font:inherit;font-weight:800;padding:.7rem 1rem}.primary:disabled{opacity:.45}.builder aside{background:var(--success-soft);border:1px solid #2c765f;border-radius:.65rem;display:grid;gap:.35rem;margin-top:1rem;padding:.8rem}.builder aside span{color:var(--text-muted);font-size:.72rem;line-height:1.5}.error{color:var(--danger)}.section-title{align-items:center;display:flex;justify-content:space-between}.section-title span{background:var(--accent-soft);border-radius:99px;padding:.25rem .55rem}.projects article{align-items:center;border-top:1px solid var(--border);display:flex;gap:1rem;justify-content:space-between;padding:1rem 0}.projects article.active{background:var(--accent-soft);margin:0 -.5rem;padding:1rem .5rem}.projects h3{margin:.2rem 0}.projects article small{color:var(--success);font-weight:800}.projects article p{margin:.3rem 0}.actions{display:flex;gap:.4rem}.actions button,.preview button{background:#202a3d;border:1px solid var(--border);border-radius:.5rem;color:var(--text);cursor:pointer;padding:.55rem .7rem}.preview{margin-top:1rem}.preview>div{align-items:center;display:flex;justify-content:space-between}.preview pre{background:#0b1018;border:1px solid var(--border);border-radius:.6rem;line-height:1.55;overflow:auto;padding:1rem;white-space:pre-wrap}@media(max-width:900px){.layout{grid-template-columns:1fr}.projects article{align-items:flex-start;flex-direction:column}}@media(max-width:520px){.labs{grid-template-columns:repeat(2,1fr)}.actions{flex-wrap:wrap}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PortfolioComponent {
  private readonly api = inject(PortfolioApiService);
  readonly labs = [
    ...Array.from({ length: 24 }, (_, index) => `JAVA-${String(index + 1).padStart(2, '0')}`),
    ...Array.from({ length: 24 }, (_, index) => `PYTHON-${String(index + 1).padStart(2, '0')}`),
    ...Array.from({ length: 24 }, (_, index) => `TYPESCRIPT-${String(index + 1).padStart(2, '0')}`),
    ...Array.from({ length: 12 }, (_, index) => `SPRING_BOOT-${String(index + 1).padStart(2, '0')}`),
    ...Array.from({ length: 10 }, (_, index) => `ANGULAR-${String(index + 1).padStart(2, '0')}`),
    ...Array.from({ length: 10 }, (_, index) => `SQL-${String(index + 1).padStart(2, '0')}`),
    ...Array.from({ length: 8 }, (_, index) => `DEVOPS-${String(index + 1).padStart(2, '0')}`),
    'LLM-01'
  ];
  readonly title = signal('Fondamentaux Java');
  readonly summary = signal('Un parcours pratique démontrant les bases de Java, de la JVM et du code maintenable.');
  readonly decisions = signal('Conserver une évaluation déterministe\nExécuter le code dans un conteneur isolé');
  readonly selectedLabs = signal(['JAVA-01']);
  readonly projects = signal<PortfolioProject[]>([]);
  readonly activeId = signal('');
  readonly markdown = signal('');
  readonly saving = signal(false);
  readonly error = signal('');

  constructor() { this.refresh(); }
  refresh(): void { this.api.list().subscribe({ next: (items) => this.projects.set(items), error: () => this.error.set('Le module portfolio est indisponible.') }); }
  toggleLab(code: string): void { this.selectedLabs.update((items) => items.includes(code) ? items.filter((item) => item !== code) : [...items, code]); }
  create(): void {
    this.saving.set(true); this.error.set('');
    const decisions = this.decisions().split(/\r?\n/).map((item) => item.trim()).filter(Boolean);
    this.api.create(this.title(), this.summary(), this.selectedLabs(), decisions).subscribe({
      next: (project) => { this.projects.update((items) => [project, ...items]); this.activeId.set(project.id); this.saving.set(false); },
      error: (response) => { this.error.set(response.error?.detail ?? 'Le projet n’a pas pu être créé.'); this.saving.set(false); }
    });
  }
  preview(project: PortfolioProject): void { this.activeId.set(project.id); this.api.readme(project.id).subscribe({ next: (text) => this.markdown.set(text), error: () => this.error.set('Aperçu indisponible.') }); }
  download(project: PortfolioProject): void {
    this.activeId.set(project.id);
    this.api.export(project.id).subscribe({ next: (blob) => { const url = URL.createObjectURL(blob); const link = document.createElement('a'); link.href = url; link.download = `${project.slug}.zip`; link.click(); URL.revokeObjectURL(url); }, error: () => this.error.set('Export indisponible.') });
  }
  value(event: Event): string { return (event.target as HTMLInputElement | HTMLTextAreaElement).value; }
}
