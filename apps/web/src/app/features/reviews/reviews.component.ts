import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { ReviewApiService, ReviewItem } from '../../core/api/review-api.service';

@Component({
  selector: 'dlr-reviews',
  imports: [DatePipe, RouterLink],
  template: `
    <header><p>Répétition espacée</p><h1>Mes révisions</h1><span>Cycle déterministe J+1, J+3, J+7, J+14 et J+30.</span></header>
    @if (loading()) { <p class="state">Chargement des révisions…</p> }
    @if (error()) { <p class="state error" role="alert">{{ error() }}</p> }
    @if (!loading() && reviews().length === 0) { <section class="empty"><strong>Tout est à jour</strong><p>Une révision apparaîtra ici lorsqu’un concept devra être consolidé.</p><a routerLink="/paths">Retour au parcours</a></section> }
    <section class="review-list">
      @for (review of reviews(); track review.id) {
        <article>
          <div class="stage">J{{ stageDay(review.stage) }}</div>
          <div><p>{{ review.labCode }} · étape {{ review.stage + 1 }}</p><h2>{{ review.reason }}</h2><span>Échéance {{ review.dueAt | date:'dd/MM/yyyy à HH:mm' }}</span></div>
          <div class="actions"><a [routerLink]="['/labs', review.labCode]">Revoir le labo</a><button type="button" (click)="finish(review, true)">Réussi</button><button class="retry" type="button" (click)="finish(review, false)">Encore difficile</button></div>
        </article>
      }
    </section>
  `,
  styles: [`
    :host{display:block} header{margin-bottom:1.5rem} header p{color:var(--accent);font-size:.78rem;font-weight:750;letter-spacing:.09em;text-transform:uppercase} header h1{font-size:clamp(2rem,4vw,3rem);margin:.2rem 0} header span,article span,article p,.empty p{color:var(--text-muted)} .review-list{display:grid;gap:1rem} article{align-items:center;background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);display:grid;gap:1rem;grid-template-columns:auto 1fr auto;padding:1rem} .stage{align-items:center;background:var(--accent-soft);border-radius:50%;color:#a9c8ff;display:flex;font-weight:850;height:3.4rem;justify-content:center;width:3.4rem} article p{font-size:.72rem;margin:0} article h2{font-size:1rem;margin:.25rem 0} .actions{display:flex;flex-wrap:wrap;gap:.5rem;justify-content:flex-end}.actions a,.actions button,.empty a{border:1px solid var(--border);border-radius:.55rem;color:var(--text);font:inherit;font-size:.78rem;font-weight:700;padding:.55rem .7rem;text-decoration:none}.actions button{background:var(--success);border-color:transparent;color:#07150d;cursor:pointer}.actions .retry{background:transparent;border-color:#99712c;color:#e8c77f}.state,.empty{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1rem}.error{color:var(--danger)}@media(max-width:760px){article{align-items:start;grid-template-columns:auto 1fr}.actions{grid-column:1/-1;justify-content:flex-start}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewsComponent {
  private readonly api = inject(ReviewApiService);
  readonly reviews = signal<ReviewItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() { void this.reload(); }

  stageDay(stage: number): number { return [1, 3, 7, 14, 30][Math.min(stage, 4)]; }

  async finish(review: ReviewItem, successful: boolean): Promise<void> {
    try {
      await firstValueFrom(this.api.complete(review.id, successful));
      await this.reload();
    } catch { this.error.set('Impossible d’enregistrer cette révision.'); }
  }

  private async reload(): Promise<void> {
    this.loading.set(true);
    try { this.reviews.set(await firstValueFrom(this.api.pending())); this.error.set(null); }
    catch { this.error.set('L’API DLR est indisponible.'); }
    finally { this.loading.set(false); }
  }
}
