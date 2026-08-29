import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, map, of, startWith } from 'rxjs';

import { DashboardApiService } from '../../core/api/dashboard-api.service';

@Component({
  selector: 'dlr-dashboard',
  imports: [AsyncPipe, DatePipe, RouterLink],
  template: `
    @if (state$ | async; as state) {
      @if (state.status === 'loading') {
        <p class="state">Chargement de ta progression…</p>
      } @else if (state.status === 'error') {
        <section class="state error"><strong>API indisponible</strong><span>Démarre PostgreSQL et le backend DLR.</span></section>
      } @else {
        <header class="hero">
          <div><p>Bonjour {{ state.data.profile.displayName }}</p><h1>Prêt pour la prochaine étape ?</h1><span>{{ state.data.profile.weekdayMinutes }} minutes prévues aujourd’hui</span></div>
          @if (state.data.nextLabCode) { <a [routerLink]="['/labs', state.data.nextLabCode]">Continuer {{ state.data.nextLabCode }} →</a> }
        </header>
        <section class="metrics" aria-label="Indicateurs de progression">
          <article><span>Progression Java</span><strong>{{ state.data.progressPercent }} %</strong><small>{{ state.data.completedLabs }} / {{ state.data.totalLabs }} laboratoires</small></article>
          <article><span>Score moyen</span><strong>{{ state.data.averageScore }} %</strong><small>Tentatives terminées</small></article>
          <article><span>Niveau</span><strong>{{ state.data.level }}</strong><small>{{ state.data.xp }} XP</small></article>
          <article><span>Révisions</span><strong>{{ state.data.pendingReviews }}</strong><small>En attente</small></article>
        </section>
        <section class="progress-panel">
          <div><h2>Parcours V1</h2><a routerLink="/paths">Voir les six laboratoires</a></div>
          <div class="track"><span [style.width.%]="state.data.progressPercent"></span></div>
        </section>
        <section class="activity">
          <h2>Activité récente</h2>
          @if (state.data.recentAttempts.length === 0) {
            <p>Aucune tentative pour le moment. Commence par JAVA-01.</p>
          } @else {
            @for (attempt of state.data.recentAttempts; track attempt.startedAt) {
              <a [routerLink]="['/labs', attempt.labCode]"><strong>{{ attempt.labCode }}</strong><span>{{ attempt.status }} · {{ attempt.startedAt | date:'dd/MM HH:mm' }}</span><b>{{ attempt.score === null ? '—' : attempt.score + ' %' }}</b></a>
            }
          }
        </section>
      }
    }
  `,
  styles: [`
    :host { display:block; } .hero { align-items:center; background:linear-gradient(135deg,var(--accent-soft),var(--surface)); border:1px solid var(--border); border-radius:var(--radius); display:flex; justify-content:space-between; padding:clamp(1.2rem,3vw,2rem); }
    .hero p { color:var(--accent); font-weight:750; margin:0; } .hero h1 { font-size:clamp(1.7rem,3vw,2.7rem); margin:.35rem 0; } .hero span,.metrics span,.metrics small,.activity span,.activity p { color:var(--text-muted); }
    .hero a,.progress-panel a { color:#b8d1ff; font-weight:750; text-decoration:none; } .metrics { display:grid; gap:1rem; grid-template-columns:repeat(4,1fr); margin:1rem 0; }
    .metrics article,.progress-panel,.activity { background:var(--surface); border:1px solid var(--border); border-radius:var(--radius); padding:1.15rem; } .metrics article { display:grid; gap:.35rem; } .metrics strong { font-size:1.75rem; }
    .progress-panel>div:first-child { align-items:center; display:flex; justify-content:space-between; } h2 { font-size:1rem; margin:0 0 1rem; } .track { background:var(--background); border-radius:999px; height:.7rem; overflow:hidden; } .track span { background:linear-gradient(90deg,var(--accent),#7aa7ff); display:block; height:100%; }
    .activity { margin-top:1rem; } .activity>a { align-items:center; border-top:1px solid var(--border); color:var(--text); display:grid; gap:1rem; grid-template-columns:auto 1fr auto; padding:.8rem 0; text-decoration:none; } .activity b { color:var(--success); }
    .state { background:var(--surface); border:1px solid var(--border); border-radius:var(--radius); padding:1rem; } .state.error { color:var(--danger); display:grid; gap:.35rem; }
    @media(max-width:900px){.metrics{grid-template-columns:repeat(2,1fr)}} @media(max-width:620px){.hero{align-items:flex-start;flex-direction:column;gap:1rem}.metrics{grid-template-columns:1fr 1fr}.activity>a{grid-template-columns:1fr auto}.activity span{grid-column:1/-1}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
  private readonly api = inject(DashboardApiService);
  readonly state$ = this.api.get().pipe(
    map((data) => ({ status: 'loaded' as const, data })),
    startWith({ status: 'loading' as const, data: null }),
    catchError(() => of({ status: 'error' as const, data: null }))
  );
}
