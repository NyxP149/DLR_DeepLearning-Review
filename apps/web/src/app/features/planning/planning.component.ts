import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { CalendarView, PlanningApiService, StudySession } from '../../core/api/planning-api.service';

@Component({
  selector: 'dlr-planning',
  imports: [DatePipe],
  template: `
    <header><p>Rythme personnel</p><h1>Planning</h1><span>Prévu et réalisé restent distincts pour garder un rythme soutenable.</span></header>
    @if (loading()) { <p class="state">Chargement du planning…</p> }
    @if (error()) { <p class="state error" role="alert">{{ error() }}</p> }
    @if (calendar(); as data) {
      <section class="summary">
        <article><strong>{{ formatDuration(data.plannedMinutes) }}</strong><span>prévues sur 14 jours</span></article>
        <article><strong>{{ formatDuration(data.actualMinutes) }}</strong><span>déjà réalisées</span></article>
        <article><strong>{{ completion(data) }} %</strong><span>du temps prévu réalisé</span></article>
      </section>
      <section class="sessions" aria-label="Séances des quatorze prochains jours">
        @for (session of data.sessions; track session.date) {
          <article [class.done]="session.status === 'COMPLETED'">
            <div class="date"><strong>{{ session.date | date:'EEE' }}</strong><span>{{ session.date | date:'dd MMM' }}</span></div>
            <div class="plan"><span>Objectif</span><strong>{{ session.plannedMinutes }} min</strong></div>
            @if (session.status === 'PLANNED') {
              <label>Réalisé <input type="number" min="0" max="480" [value]="minutes[session.date]" (input)="setMinutes(session.date, $event)" aria-label="Minutes réalisées"></label>
              <label>Récompense <input maxlength="120" [value]="rewards[session.date] || ''" (input)="setReward(session.date, $event)" placeholder="Après l'effort…"></label>
              <button type="button" (click)="save(session)" [disabled]="saving() === session.date">{{ saving() === session.date ? 'Enregistrement…' : 'Clôturer' }}</button>
            } @else {
              <div class="result"><span>{{ session.status === 'COMPLETED' ? 'Réalisé' : 'Repos / report' }}</span><strong>{{ session.actualMinutes }} min</strong><small>{{ session.reward || 'Aucune récompense indiquée' }}</small></div>
            }
          </article>
        }
      </section>
    }
  `,
  styles: [`
    :host{display:block}header{margin-bottom:1.5rem}header p{color:var(--accent);font-size:.78rem;font-weight:750;letter-spacing:.09em;text-transform:uppercase}header h1{font-size:clamp(2rem,4vw,3rem);margin:.2rem 0}header span,.summary span,.date span,.plan span,label,.result span,.result small{color:var(--text-muted)}.summary{display:grid;gap:1rem;grid-template-columns:repeat(3,1fr);margin-bottom:1rem}.summary article,.sessions article{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius)}.summary article{display:grid;padding:1rem}.summary strong{font-size:1.45rem}.summary span{font-size:.75rem}.sessions{display:grid;gap:.7rem}.sessions article{align-items:center;display:grid;gap:1rem;grid-template-columns:5rem 6rem minmax(7rem,9rem) minmax(12rem,1fr) auto;padding:.85rem 1rem}.sessions article.done{border-color:#24583b}.date,.plan,.result{display:grid}.date strong{text-transform:capitalize}.date span,.plan span,label,.result span,.result small{font-size:.72rem}label{display:grid;gap:.25rem}input{background:var(--background);border:1px solid var(--border);border-radius:.5rem;color:var(--text);font:inherit;padding:.5rem;width:100%}button{background:var(--accent);border:0;border-radius:.55rem;color:white;cursor:pointer;font:inherit;font-size:.78rem;font-weight:750;padding:.6rem .8rem}button:disabled{cursor:wait;opacity:.6}.result{grid-column:3/-1}.state{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1rem}.error{color:var(--danger)}@media(max-width:900px){.sessions article{grid-template-columns:4rem 1fr 1fr}.sessions label:nth-of-type(2){grid-column:2/4}.sessions button{grid-column:2/4}.result{grid-column:2/4}}@media(max-width:620px){.summary{grid-template-columns:1fr}.sessions article{grid-template-columns:4rem 1fr}.sessions label,.sessions label:nth-of-type(2),.sessions button,.result{grid-column:1/-1}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlanningComponent {
  private readonly api = inject(PlanningApiService);
  readonly calendar = signal<CalendarView | null>(null);
  readonly loading = signal(true);
  readonly saving = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly minutes: Record<string, number> = {};
  readonly rewards: Record<string, string> = {};

  constructor() { void this.reload(); }

  formatDuration(minutes: number): string { return `${Math.floor(minutes / 60)} h ${minutes % 60}`; }
  completion(data: CalendarView): number { return data.plannedMinutes ? Math.round(data.actualMinutes * 100 / data.plannedMinutes) : 0; }
  setMinutes(date: string, event: Event): void { this.minutes[date] = Number((event.target as HTMLInputElement).value); }
  setReward(date: string, event: Event): void { this.rewards[date] = (event.target as HTMLInputElement).value; }

  async save(session: StudySession): Promise<void> {
    const actual = Number(this.minutes[session.date] ?? session.plannedMinutes);
    if (!Number.isInteger(actual) || actual < 0 || actual > 480) {
      this.error.set('Indique un nombre de minutes compris entre 0 et 480.'); return;
    }
    this.saving.set(session.date);
    try {
      await firstValueFrom(this.api.record(session.date, actual, this.rewards[session.date] ?? ''));
      await this.reload();
    } catch { this.error.set('Impossible d’enregistrer cette séance.'); }
    finally { this.saving.set(null); }
  }

  private async reload(): Promise<void> {
    this.loading.set(true);
    try {
      const data = await firstValueFrom(this.api.get());
      this.calendar.set(data);
      for (const session of data.sessions) this.minutes[session.date] = session.plannedMinutes;
      this.error.set(null);
    } catch { this.error.set('L’API DLR est indisponible.'); }
    finally { this.loading.set(false); }
  }
}
