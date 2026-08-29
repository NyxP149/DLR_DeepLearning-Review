import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'dlr-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app-shell">
      <aside class="sidebar" aria-label="Navigation principale">
        <a class="brand" routerLink="/">DLR<span>.</span></a>
        <nav>
          <a routerLink="/paths" routerLinkActive="active">Mon parcours</a>
          <a routerLink="/labs/JAVA-01" routerLinkActive="active">Laboratoire</a>
          <span class="disabled" aria-disabled="true">Concepts clés</span>
          <span class="disabled" aria-disabled="true">Calendrier</span>
        </nav>
        <p class="build-label">DLR V1 · Java 01–06</p>
      </aside>
      <main>
        <router-outlet />
      </main>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {}
