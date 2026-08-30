import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { DashboardApiService } from './core/api/dashboard-api.service';

@Component({
  selector: 'dlr-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app-frame">
      <header class="topbar">
        <a class="brand" routerLink="/dashboard" aria-label="Accueil DLR">
          <span class="brand-mark">D</span>
          <span class="brand-copy"><strong>DLR</strong><small>Deep Learning &amp; Review</small></span>
        </a>
        <p class="today">Apprentissage local · objectif du jour : {{ dailyMinutes() }} minutes</p>
        <a class="top-action" routerLink="/settings">Paramètres</a>
        <span class="avatar" [attr.aria-label]="'Profil ' + displayName()">{{ initials() }}</span>
      </header>
      <div class="app-shell">
        <aside class="sidebar" aria-label="Navigation principale">
        <nav>
          <a routerLink="/dashboard" routerLinkActive="active">Tableau de bord</a>
          <a routerLink="/paths" routerLinkActive="active">Mon parcours</a>
          <a routerLink="/labs/JAVA-01" routerLinkActive="active">Laboratoire</a>
          <a routerLink="/concepts" routerLinkActive="active">Concepts clés</a>
          <a routerLink="/reviews" routerLinkActive="active">Révisions</a>
          <a routerLink="/planning" routerLinkActive="active">Planning</a>
          <a routerLink="/coach" routerLinkActive="active">Coach V2</a>
          <a routerLink="/portfolio" routerLinkActive="active">Portfolio</a>
          <a routerLink="/settings" routerLinkActive="active">Paramètres</a>
        </nav>
          <div class="side-goal">
            <small>Objectif V2</small>
        <strong>113 activités disponibles</strong>
            <span><i [style.width.%]="goalProgress()"></i></span>
          </div>
          <p class="build-label">DLR V2 · local-first</p>
        </aside>
        <main>
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  private readonly dashboard = inject(DashboardApiService);
  readonly goalProgress = signal(0);
  readonly dailyMinutes = signal(90);
  readonly displayName = signal('Apprenant DLR');

  constructor() {
    this.dashboard.get().subscribe({
      next: (data) => {
        this.goalProgress.set(data.progressPercent);
        this.dailyMinutes.set(data.profile.weekdayMinutes);
        this.displayName.set(data.profile.displayName);
      }
    });
  }

  initials(): string {
    return this.displayName().split(/\s+/).filter(Boolean).slice(0, 2)
      .map((part) => part[0]).join('').toUpperCase() || 'DL';
  }
}
