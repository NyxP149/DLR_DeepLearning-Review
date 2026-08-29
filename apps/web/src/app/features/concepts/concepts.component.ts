import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, map, of, startWith } from 'rxjs';

import { ConceptMastery, MasteryApiService, MasteryStatus } from '../../core/api/mastery-api.service';

@Component({
  selector: 'dlr-concepts',
  imports: [AsyncPipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Carte de maîtrise</p>
      <h1>Concepts clés</h1>
      <span>Comprendre, pratiquer, puis consolider avec les révisions espacées.</span>
    </header>

    @if (state$ | async; as state) {
      @if (state.status === 'loading') {
        <p class="state">Chargement des concepts…</p>
      } @else if (state.status === 'error') {
        <p class="state error" role="alert">L’API DLR est indisponible.</p>
      } @else {
        <section class="summary" aria-label="Résumé de maîtrise">
          <article><strong>{{ count(state.concepts, 'MASTERED') }}</strong><span>maîtrisés</span></article>
          <article><strong>{{ count(state.concepts, 'CONSOLIDATING') }}</strong><span>en consolidation</span></article>
          <article><strong>{{ count(state.concepts, 'TO_REVIEW') }}</strong><span>à revoir</span></article>
          <article><strong>{{ count(state.concepts, 'NOT_STARTED') }}</strong><span>à découvrir</span></article>
        </section>

        <section class="concept-grid" aria-label="Concepts Java">
          @for (concept of state.concepts; track concept.code) {
            <article class="concept-card">
              <div class="meta">
                <span>{{ concept.labCode }}</span>
                <span class="badge" [class]="'badge ' + concept.status.toLowerCase()">{{ statusLabel(concept.status) }}</span>
              </div>
              <h2>{{ concept.name }}</h2>
              <p>{{ concept.definition }}</p>
              <div class="progress" role="progressbar" [attr.aria-label]="'Maîtrise de ' + concept.name" [attr.aria-valuenow]="progress(concept)" aria-valuemin="0" aria-valuemax="100">
                <span [style.width.%]="progress(concept)"></span>
              </div>
              <small>{{ progressLabel(concept) }}</small>
              <details>
                <summary>Approfondir</summary>
                <h3>Pourquoi ce concept existe</h3><p>{{ concept.whyExists }}</p>
                <h3>Pourquoi il est important</h3><p>{{ concept.whyImportant }}</p>
                <h3>Exemple minimal</h3><pre><code>{{ concept.minimalExample }}</code></pre>
                <h3>Erreur fréquente</h3><p>{{ concept.commonMistake }}</p>
                <h3>Question de maîtrise</h3><p>{{ concept.masteryQuestion }}</p>
                <h3>Preuve attendue</h3><p>{{ concept.masteryProof }}</p>
              </details>
              <a [routerLink]="['/labs', concept.labCode]">Ouvrir le laboratoire →</a>
            </article>
          }
        </section>
      }
    }
  `,
  styles: [`
    :host{display:block}.page-header{margin-bottom:1.5rem}.page-header p{color:var(--accent);font-size:.78rem;font-weight:750;letter-spacing:.09em;text-transform:uppercase}.page-header h1{font-size:clamp(2rem,4vw,3rem);margin:.2rem 0}.page-header span,.concept-card p,.concept-card small{color:var(--text-muted)}.summary{display:grid;gap:.75rem;grid-template-columns:repeat(4,1fr);margin-bottom:1rem}.summary article{background:var(--surface);border:1px solid var(--border);border-radius:.8rem;display:grid;padding:1rem}.summary strong{font-size:1.6rem}.summary span{color:var(--text-muted);font-size:.75rem}.concept-grid{display:grid;gap:1rem;grid-template-columns:repeat(auto-fit,minmax(min(100%,320px),1fr))}.concept-card{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1.1rem}.meta{align-items:center;display:flex;justify-content:space-between}.meta>span:first-child{color:var(--accent);font-size:.72rem;font-weight:750}.badge{border-radius:99px;font-size:.67rem;font-weight:750;padding:.3rem .55rem}.not_started{background:#202938;color:#a7b2c4}.to_review{background:#392519;color:#f0b679}.consolidating{background:var(--accent-soft);color:#a9c8ff}.mastered{background:var(--success-soft);color:var(--success)}h2{font-size:1.15rem;margin:.8rem 0 .45rem}.progress{background:#202938;border-radius:99px;height:.4rem;margin-top:1rem;overflow:hidden}.progress span{background:var(--accent);display:block;height:100%}small{display:block;margin-top:.45rem}details{border-top:1px solid var(--border);margin-top:1rem;padding-top:.8rem}summary{cursor:pointer;font-weight:700}h3{font-size:.8rem;margin:1rem 0 .25rem}pre{background:#0b1018;border:1px solid var(--border);border-radius:.55rem;overflow:auto;padding:.7rem;white-space:pre-wrap}.concept-card>a{color:#a9c8ff;display:inline-block;font-size:.8rem;font-weight:750;margin-top:1rem;text-decoration:none}.state{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1rem}.error{color:var(--danger)}@media(max-width:760px){.summary{grid-template-columns:repeat(2,1fr)}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConceptsComponent {
  private readonly api = inject(MasteryApiService);
  readonly state$ = this.api.listConcepts().pipe(
    map((concepts) => ({ status: 'loaded' as const, concepts })),
    startWith({ status: 'loading' as const, concepts: [] as ConceptMastery[] }),
    catchError(() => of({ status: 'error' as const, concepts: [] as ConceptMastery[] }))
  );

  count(concepts: ConceptMastery[], status: MasteryStatus): number {
    return concepts.filter((concept) => concept.status === status).length;
  }

  statusLabel(status: MasteryStatus): string {
    return ({ NOT_STARTED: 'À découvrir', TO_REVIEW: 'À revoir', CONSOLIDATING: 'En consolidation', MASTERED: 'Maîtrisé' })[status];
  }

  progress(concept: ConceptMastery): number {
    if (concept.status === 'MASTERED') return 100;
    if (concept.status === 'CONSOLIDATING') return 55 + Math.max(0, concept.completedReviewStage + 1) * 9;
    if (concept.status === 'TO_REVIEW') return Math.round(concept.score ?? 25);
    return 0;
  }

  progressLabel(concept: ConceptMastery): string {
    if (concept.status === 'NOT_STARTED') return 'Aucune tentative terminée';
    if (concept.status === 'MASTERED') return 'Cycle J+30 validé';
    if (concept.status === 'TO_REVIEW') return `Dernier meilleur score : ${concept.score ?? 0} %`;
    return concept.completedReviewStage < 0
      ? `Score ${concept.score} % · première révision à venir`
      : `Score ${concept.score} % · étape J${[1, 3, 7, 14, 30][Math.min(concept.completedReviewStage, 4)]} validée`;
  }
}
