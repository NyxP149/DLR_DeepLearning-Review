import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import {
  AdaptationApiService,
  AdaptationDecision,
  AdaptationInsights,
  AdaptationRecommendation
} from '../../core/api/adaptation-api.service';
import { TutorApiService, TutorRole } from '../../core/api/tutor-api.service';

@Component({
  selector: 'dlr-coach',
  imports: [DatePipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Coach adaptatif V2</p>
      <h1>Ta prochaine action, expliquée</h1>
      <span>DLR propose. Tu gardes toujours le contrôle du parcours.</span>
    </header>

    @if (loading()) {
      <p class="state">Analyse locale de ta progression…</p>
    } @else if (error()) {
      <p class="state error" role="alert">{{ error() }}</p>
    } @else {
      @if (recommendation(); as item) {
        <section class="recommendation">
          <div class="eyebrow"><span>{{ difficultyLabel(item.difficulty) }}</span><small>Expire le {{ item.expiresAt | date:'dd/MM à HH:mm' }}</small></div>
          <h2>{{ item.targetedConcepts.join(' · ') }}</h2>
          <p class="reason">{{ item.reason }}</p>
          <article><small>Activité proposée</small><strong>{{ item.proposedActivity }}</strong></article>
          <article><small>Bénéfice attendu</small><strong>{{ item.expectedBenefit }}</strong></article>
          <details open><summary>Pourquoi cette proposition ?</summary><ul>@for (factor of item.factors; track factor) { <li>{{ factor }}</li> }</ul></details>
          <div class="actions">
            <button class="primary" type="button" (click)="decide('ACCEPT')" [disabled]="deciding()">Accepter</button>
            <button type="button" (click)="decide('POSTPONE')" [disabled]="deciding()">Reporter 3 jours</button>
            <button type="button" (click)="decide('REPLACE')" [disabled]="deciding()">Autre proposition</button>
            <button class="quiet" type="button" (click)="decide('IGNORE')" [disabled]="deciding()">Ignorer</button>
            <a [routerLink]="['/labs', item.labCode]">Ouvrir {{ item.labCode }} →</a>
          </div>
          @if (feedback()) { <p class="feedback" role="status">{{ feedback() }}</p> }
        </section>
      }

      @if (insights(); as data) {
        <section class="analytics">
          <div class="section-title"><div><p>Signaux locaux</p><h2>Autonomie et transfert</h2></div><span>Estimations, jamais des verdicts</span></div>
          <div class="metrics">
            <article><span>Autonomie</span><strong>{{ data.autonomyScore }} %</strong><i><b [style.width.%]="data.autonomyScore"></b></i></article>
            <article><span>Dépendance aux indices</span><strong>{{ data.hintDependencyPercent }} %</strong><i><b [style.width.%]="data.hintDependencyPercent"></b></i></article>
            <article><span>Transfert confirmé</span><strong>{{ data.transferScore }} %</strong><i><b [style.width.%]="data.transferScore"></b></i></article>
            <article><span>Reste estimé</span><strong>{{ data.estimatedWeeksRemaining }} sem.</strong><small>au rythme récent</small></article>
          </div>
          <ul>@for (factor of data.factors; track factor) { <li>{{ factor }}</li> }</ul>
          <p class="disclaimer">{{ data.disclaimer }}</p>
        </section>
      }
      <section class="role-lab">
        <div class="section-title"><div><p>Tuteur multi-rôle</p><h2>Choisis la posture utile</h2></div><span>Ollama local · aucun score modifié</span></div>
        <div class="roles" role="radiogroup" aria-label="Rôle du tuteur">
          @for (option of roles; track option.value) {
            <button type="button" [class.active]="role() === option.value" (click)="role.set(option.value)"><strong>{{ option.label }}</strong><small>{{ option.description }}</small></button>
          }
        </div>
        <div class="consult-form">
          <label>Laboratoire<input [value]="labCode()" (input)="labCode.set(asInput($event).value)" maxlength="40"></label>
          <label>Question<textarea [value]="question()" (input)="question.set(asInput($event).value)" maxlength="2000" rows="3"></textarea></label>
          <label>Contexte facultatif<textarea [value]="context()" (input)="context.set(asInput($event).value)" maxlength="6000" rows="4" placeholder="Colle ici ton raisonnement ou un extrait de code pertinent."></textarea></label>
          <button class="ask" type="button" (click)="consult()" [disabled]="consulting() || !question().trim()">{{ consulting() ? 'Réflexion locale…' : 'Consulter ce rôle' }}</button>
        </div>
        @if (tutorError()) { <p class="state error" role="alert">{{ tutorError() }}</p> }
        @if (tutorAnswer()) { <article class="answer"><small>{{ roleLabel(role()) }} · réponse locale</small><p>{{ tutorAnswer() }}</p></article> }
      </section>
    }
  `,
  styles: [`
    :host{display:block}.page-header{margin-bottom:1.4rem}.page-header p,.section-title p{color:var(--accent);font-size:.75rem;font-weight:800;letter-spacing:.09em;margin:0;text-transform:uppercase}.page-header h1{font-size:clamp(2rem,4vw,3rem);margin:.25rem 0}.page-header span,.eyebrow small,.section-title span,.metrics span,.metrics small,.disclaimer{color:var(--text-muted)}.recommendation,.analytics,.role-lab,.state{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:clamp(1rem,3vw,1.5rem)}.recommendation{box-shadow:inset 4px 0 var(--accent)}.eyebrow,.section-title{align-items:center;display:flex;justify-content:space-between}.eyebrow>span{background:var(--accent-soft);border-radius:99px;color:var(--link);font-size:.7rem;font-weight:800;padding:.35rem .65rem}.recommendation h2{font-size:1.4rem;margin:1rem 0 .35rem}.reason{color:var(--text);font-size:1.05rem;line-height:1.6}.recommendation article{background:var(--background);border:1px solid var(--border);border-radius:.7rem;display:grid;gap:.35rem;margin-top:.7rem;padding:.85rem}.recommendation article small{color:var(--accent);font-weight:750}.recommendation article strong{line-height:1.5}.recommendation details{border-top:1px solid var(--border);margin-top:1rem;padding-top:1rem}.recommendation summary{cursor:pointer;font-weight:750}.recommendation li,.analytics li{color:var(--text-muted);margin:.35rem 0}.actions{align-items:center;display:flex;flex-wrap:wrap;gap:.55rem;margin-top:1.1rem}.actions button,.actions a{background:var(--surface-raised);border:1px solid var(--border);border-radius:.55rem;color:var(--text);cursor:pointer;font:inherit;font-size:.78rem;font-weight:750;padding:.65rem .85rem;text-decoration:none}.actions button.primary{background:var(--accent);border-color:var(--accent);color:var(--on-accent)}.actions button.quiet{background:transparent;color:var(--text-muted)}.actions a{margin-left:auto}.actions button:disabled{cursor:wait;opacity:.55}.feedback{color:var(--success);font-size:.8rem;margin-bottom:0}.analytics,.role-lab{margin-top:1rem}.section-title h2{margin:.2rem 0}.metrics{display:grid;gap:.75rem;grid-template-columns:repeat(4,1fr);margin-top:1rem}.metrics article{background:var(--background);border:1px solid var(--border);border-radius:.7rem;display:grid;gap:.4rem;padding:.85rem}.metrics strong{font-size:1.55rem}.metrics i{background:var(--track);border-radius:99px;height:.35rem;overflow:hidden}.metrics b{background:linear-gradient(90deg,var(--accent),var(--success));display:block;height:100%}.disclaimer{border-top:1px solid var(--border);font-size:.72rem;margin:1rem 0 0;padding-top:.8rem}.roles{display:grid;gap:.6rem;grid-template-columns:repeat(5,1fr);margin-top:1rem}.roles button{background:var(--background);border:1px solid var(--border);border-radius:.7rem;color:var(--text);cursor:pointer;display:grid;gap:.35rem;padding:.8rem;text-align:left}.roles button.active{background:var(--accent-soft);border-color:var(--accent)}.roles small{color:var(--text-muted);font-size:.68rem}.consult-form{display:grid;gap:.8rem;margin-top:1rem}.consult-form label{color:var(--text-muted);display:grid;font-size:.76rem;font-weight:750;gap:.35rem}.consult-form input,.consult-form textarea{background:var(--input-background);border:1px solid var(--border);border-radius:.55rem;color:var(--text);font:inherit;padding:.7rem;resize:vertical}.ask{background:var(--accent);border:0;border-radius:.55rem;color:var(--on-accent);cursor:pointer;font:inherit;font-weight:800;justify-self:start;padding:.7rem 1rem}.ask:disabled{opacity:.5}.answer{background:var(--success-soft);border:1px solid var(--success);border-radius:.7rem;margin-top:1rem;padding:1rem}.answer small{color:var(--success);font-weight:800}.answer p{line-height:1.65;white-space:pre-wrap}.state.error{color:var(--danger);margin-top:1rem}@media(max-width:1000px){.roles{grid-template-columns:repeat(3,1fr)}}@media(max-width:850px){.metrics{grid-template-columns:1fr 1fr}}@media(max-width:600px){.eyebrow,.section-title{align-items:flex-start;flex-direction:column;gap:.55rem}.metrics,.roles{grid-template-columns:1fr}.actions a{margin-left:0;width:100%}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CoachComponent {
  private readonly api = inject(AdaptationApiService);
  private readonly tutor = inject(TutorApiService);
  readonly loading = signal(true);
  readonly deciding = signal(false);
  readonly error = signal('');
  readonly feedback = signal('');
  readonly recommendation = signal<AdaptationRecommendation | null>(null);
  readonly insights = signal<AdaptationInsights | null>(null);
  readonly role = signal<TutorRole>('TEACHER');
  readonly labCode = signal('JAVA-01');
  readonly question = signal('Comment puis-je mieux comprendre ce laboratoire ?');
  readonly context = signal('');
  readonly consulting = signal(false);
  readonly tutorAnswer = signal('');
  readonly tutorError = signal('');
  readonly roles: { value: TutorRole; label: string; description: string }[] = [
    { value: 'TEACHER', label: 'Professeur', description: 'Explique et vérifie' },
    { value: 'COACH', label: 'Coach', description: 'Débloque la prochaine action' },
    { value: 'REVIEWER', label: 'Reviewer', description: 'Relit avec exigence' },
    { value: 'CLIENT', label: 'Client', description: 'Clarifie le besoin' },
    { value: 'TECH_LEAD', label: 'Tech lead', description: 'Challenge les compromis' }
  ];

  constructor() {
    forkJoin({ recommendation: this.api.recommendation(), insights: this.api.insights() }).subscribe({
      next: ({ recommendation, insights }) => {
        this.recommendation.set(recommendation);
        this.insights.set(insights);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Le coach local est indisponible. Vérifie que l’API DLR est démarrée.');
        this.loading.set(false);
      }
    });
  }

  decide(decision: AdaptationDecision): void {
    const item = this.recommendation();
    if (!item || this.deciding()) return;
    this.deciding.set(true);
    this.feedback.set('');
    this.api.decide(item.id, decision).subscribe({
      next: (recommendation) => {
        this.recommendation.set(recommendation);
        this.feedback.set(({ ACCEPT: 'Proposition acceptée.', POSTPONE: 'Proposition reportée de trois jours.', REPLACE: 'Nouvelle proposition générée.', IGNORE: 'Proposition ignorée.' })[decision]);
        this.deciding.set(false);
      },
      error: () => {
        this.feedback.set('La décision n’a pas pu être enregistrée. Recharge la page.');
        this.deciding.set(false);
      }
    });
  }

  difficultyLabel(difficulty: AdaptationRecommendation['difficulty']): string {
    return ({ GUIDE: 'Découverte guidée', RENFORCEMENT: 'Renforcement', TRANSFERT: 'Mise en transfert', CHALLENGE: 'Défi autonome' })[difficulty];
  }

  consult(): void {
    if (this.consulting() || !this.question().trim()) return;
    this.consulting.set(true);
    this.tutorAnswer.set('');
    this.tutorError.set('');
    this.tutor.consult(this.role(), this.labCode().trim(), this.context(), this.question()).subscribe({
      next: (answer) => { this.tutorAnswer.set(answer.content); this.consulting.set(false); },
      error: () => { this.tutorError.set('Ollama est indisponible. Le parcours et les scores restent pleinement utilisables.'); this.consulting.set(false); }
    });
  }

  roleLabel(role: TutorRole): string {
    return this.roles.find((option) => option.value === role)?.label ?? role;
  }

  asInput(event: Event): HTMLInputElement | HTMLTextAreaElement {
    return event.target as HTMLInputElement | HTMLTextAreaElement;
  }
}
