import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { ReviewAnswerResult, ReviewApiService, ReviewItem, ReviewQuestion } from '../../core/api/review-api.service';

@Component({
  selector: 'dlr-reviews',
  imports: [DatePipe, RouterLink],
  template: `
    <header><p>Répétition espacée</p><h1>Mes révisions</h1><span>Une question de rappel, sans le cours sous les yeux. Le résultat et ta confiance règlent la prochaine échéance (J+1, J+3, J+7, J+14, J+30).</span></header>
    @if (loading()) { <p class="state">Chargement des révisions…</p> }
    @if (error()) { <p class="state error" role="alert">{{ error() }}</p> }
    @if (!loading() && reviews().length === 0) { <section class="empty"><strong>Tout est à jour</strong><p>Une révision apparaîtra ici lorsqu’un concept devra être consolidé.</p><a routerLink="/paths">Retour au parcours</a></section> }
    <section class="review-list">
      @for (review of reviews(); track review.id) {
        <article [class.open]="openId() === review.id">
          <div class="stage">J{{ stageDay(review.stage) }}</div>
          <div><p>{{ review.labCode }} · étape {{ review.stage + 1 }}</p><h2>{{ review.reason }}</h2><span>{{ isDue(review) ? 'Échue' : 'Prévue' }} · {{ review.dueAt | date:'dd/MM/yyyy à HH:mm' }}</span></div>
          <div class="actions">
            <button type="button" [disabled]="busy()" (click)="open(review)">{{ openId() === review.id ? 'Recharger la question' : 'Répondre à la question' }}</button>
          </div>
          @if (review.belowThreshold) { <p class="gate">Score du laboratoire : {{ review.bestScore }} %, sous le seuil. La question reste possible ; pour recalculer ton score, <a [routerLink]="['/labs', review.labCode]">revois le labo</a>.</p> }

          @if (openId() === review.id && question(); as q) {
            <section class="recall" aria-live="polite">
              <p class="eyebrow">Question de rappel · sans le cours</p>
              <h3>{{ q.prompt }}</h3>
              @if (!result()) {
                @if (q.type === 'SINGLE_CHOICE') {
                  <div class="choices" role="radiogroup" [attr.aria-label]="q.prompt">
                    @for (option of q.choices; track option; let index = $index) {
                      <label><input type="radio" name="recall-choice" [checked]="choice() === index" (change)="choice.set(index)"><span>{{ option }}</span></label>
                    }
                  </div>
                } @else {
                  <textarea rows="5" placeholder="Réponds de mémoire, avec tes propres mots…" [value]="text()" (input)="text.set(value($event))"></textarea>
                }
                <div class="confidence" role="radiogroup" aria-label="Ta confiance">
                  <span>Ta confiance, avant la correction :</span>
                  @for (level of levels; track level.value) {
                    <label><input type="radio" name="recall-confidence" [checked]="confidence() === level.value" (change)="confidence.set(level.value)"><span>{{ level.label }}</span></label>
                  }
                </div>
                <button class="primary" type="button" [disabled]="!canSubmit(q) || busy()" (click)="submit(review, q)">{{ busy() ? 'Correction…' : 'Valider ma réponse' }}</button>
              } @else {
                @if (result(); as r) {
                  <div class="verdict" [class.ok]="r.outcome === 'CORRECT'" [class.mid]="r.outcome === 'PARTIAL'">
                    <strong>{{ r.outcome === 'CORRECT' ? '✓ Acquis' : r.outcome === 'PARTIAL' ? '≈ Partiellement acquis' : '✗ À retravailler' }}</strong>
                    <p>{{ r.feedback }}</p>
                    @if (q.type === 'SINGLE_CHOICE' && r.outcome !== 'CORRECT' && r.correctChoice !== null) { <p>Bonne réponse : <em>{{ q.choices[r.correctChoice] }}</em></p> }
                    @if (r.explanation) { <p>{{ r.explanation }}</p> }
                    @if (r.modelAnswer) { <p><b>Réponse de référence :</b> {{ r.modelAnswer }}</p> }
                    <p class="calibration">{{ r.calibration }}</p>
                    @if (r.nextReview; as next) { <p class="next">Prochaine révision : {{ next.dueAt | date:'dd/MM/yyyy' }} (étape {{ next.stage + 1 }}).</p> }
                    @else { <p class="next">Cycle terminé : ce labo est consolidé.</p> }
                  </div>
                  <div class="actions"><button type="button" (click)="close()">Continuer</button><a [routerLink]="['/labs', review.labCode]">Revoir le labo</a></div>
                }
              }
            </section>
          }
        </article>
      }
    </section>
  `,
  styles: [`
    :host{display:block} header{margin-bottom:1.5rem} header p{color:var(--accent);font-size:.78rem;font-weight:750;letter-spacing:.09em;text-transform:uppercase} header h1{font-size:clamp(2rem,4vw,3rem);margin:.2rem 0} header span,article span,article p,.empty p{color:var(--text-muted)} .review-list{display:grid;gap:1rem} article{align-items:center;background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);display:grid;gap:1rem;grid-template-columns:auto 1fr auto;padding:1rem} article.open{border-color:var(--accent)} .stage{align-items:center;background:var(--accent-soft);border-radius:50%;color:#a9c8ff;display:flex;font-weight:850;height:3.4rem;justify-content:center;width:3.4rem} article p{font-size:.72rem;margin:0} article h2{font-size:1rem;margin:.25rem 0} .actions{display:flex;flex-wrap:wrap;gap:.5rem;justify-content:flex-end}.actions a,.actions button,.empty a{border:1px solid var(--border);border-radius:.55rem;color:var(--text);font:inherit;font-size:.78rem;font-weight:700;padding:.55rem .7rem;text-decoration:none}.actions button{background:var(--accent);border-color:transparent;color:#07111f;cursor:pointer}.actions button:disabled,.primary:disabled{cursor:not-allowed;opacity:.45}.gate{color:#e8c77f;grid-column:1/-1;margin:0}.gate a{color:inherit}.state,.empty{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1rem}.error{color:var(--danger)}
    .recall{border-top:1px solid var(--border);display:grid;gap:.8rem;grid-column:1/-1;padding-top:1rem}.recall .eyebrow{color:var(--accent);font-size:.7rem;font-weight:800;letter-spacing:.08em;margin:0;text-transform:uppercase}.recall h3{font-size:1.05rem;line-height:1.45;margin:0}.choices{display:grid;gap:.5rem}.choices label,.confidence label{align-items:flex-start;cursor:pointer;display:flex;gap:.5rem}.choices label{background:var(--surface-raised);border:1px solid var(--border);border-radius:.6rem;padding:.65rem}.choices input,.confidence input{accent-color:var(--accent);margin-top:.2rem}.recall textarea{background:var(--background);border:1px solid var(--border);border-radius:.6rem;color:var(--text);font:inherit;padding:.7rem;resize:vertical}.confidence{align-items:center;color:var(--text-muted);display:flex;flex-wrap:wrap;font-size:.85rem;gap:.5rem .9rem}.primary{background:var(--accent);border:0;border-radius:.6rem;color:#07111f;cursor:pointer;font:inherit;font-weight:800;justify-self:start;padding:.6rem 1rem}.verdict{background:var(--surface-raised);border:1px solid var(--danger);border-radius:.7rem;display:grid;gap:.4rem;padding:.9rem}.verdict.ok{border-color:var(--success)}.verdict.mid{border-color:#99712c}.verdict p{font-size:.85rem;margin:0}.verdict .calibration,.verdict .next{color:var(--text-muted)}
    @media(max-width:760px){article{align-items:start;grid-template-columns:auto 1fr}.actions{grid-column:1/-1;justify-content:flex-start}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewsComponent {
  private readonly api = inject(ReviewApiService);
  readonly reviews = signal<ReviewItem[]>([]);
  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly openId = signal<string | null>(null);
  readonly question = signal<ReviewQuestion | null>(null);
  readonly result = signal<ReviewAnswerResult | null>(null);
  readonly choice = signal<number | null>(null);
  readonly text = signal('');
  readonly confidence = signal<number | null>(null);
  readonly levels = [
    { value: 1, label: 'Pas sûr·e' },
    { value: 2, label: 'Plutôt sûr·e' },
    { value: 3, label: 'Sûr·e' }
  ];

  constructor() { void this.reload(); }

  stageDay(stage: number): number { return [1, 3, 7, 14, 30][Math.min(stage, 4)]; }

  isDue(review: ReviewItem): boolean { return new Date(review.dueAt).getTime() <= Date.now(); }

  value(event: Event): string { return (event.target as HTMLTextAreaElement).value; }

  canSubmit(question: ReviewQuestion): boolean {
    const answered = question.type === 'SINGLE_CHOICE' ? this.choice() !== null : this.text().trim().length > 0;
    return answered && this.confidence() !== null;
  }

  async open(review: ReviewItem): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set(null);
    try {
      this.question.set(await firstValueFrom(this.api.question(review.id)));
      this.openId.set(review.id);
      this.result.set(null);
      this.choice.set(null);
      this.text.set('');
      this.confidence.set(null);
    } catch (error) {
      this.error.set(this.detail(error, 'Impossible de charger la question de rappel.'));
    } finally {
      this.busy.set(false);
    }
  }

  async submit(review: ReviewItem, question: ReviewQuestion): Promise<void> {
    if (this.busy() || !this.canSubmit(question)) return;
    this.busy.set(true);
    try {
      const confidence = this.confidence() as number;
      this.result.set(await firstValueFrom(this.api.answer(review.id, question.type === 'SINGLE_CHOICE'
        ? { questionId: question.questionId, selectedChoice: this.choice() as number, confidence }
        : { questionId: question.questionId, answerText: this.text(), confidence })));
    } catch (error) {
      this.error.set(this.detail(error, 'Impossible d’enregistrer cette révision.'));
    } finally {
      this.busy.set(false);
    }
  }

  async close(): Promise<void> {
    this.openId.set(null);
    this.question.set(null);
    this.result.set(null);
    await this.reload();
  }

  private detail(error: unknown, fallback: string): string {
    const detail = error instanceof HttpErrorResponse ? error.error?.detail : null;
    return typeof detail === 'string' ? detail : fallback;
  }

  private async reload(clearError = true): Promise<void> {
    this.loading.set(true);
    try { this.reviews.set(await firstValueFrom(this.api.pending())); if (clearError) this.error.set(null); }
    catch { this.error.set('L’API DLR est indisponible.'); }
    finally { this.loading.set(false); }
  }
}
