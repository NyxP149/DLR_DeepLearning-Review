import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((module) => module.DashboardComponent)
  },
  {
    path: 'paths',
    loadComponent: () =>
      import('./features/paths/paths.component').then((module) => module.PathsComponent)
  },
  {
    path: 'reviews',
    loadComponent: () =>
      import('./features/reviews/reviews.component').then((module) => module.ReviewsComponent)
  },
  {
    path: 'concepts',
    loadComponent: () =>
      import('./features/concepts/concepts.component').then((module) => module.ConceptsComponent)
  },
  {
    path: 'planning',
    loadComponent: () =>
      import('./features/planning/planning.component').then((module) => module.PlanningComponent)
  },
  {
    path: 'settings',
    loadComponent: () =>
      import('./features/settings/settings.component').then((module) => module.SettingsComponent)
  },
  {
    path: 'labs/:code',
    loadComponent: () =>
      import('./features/lab-workspace/lab-workspace.component').then(
        (module) => module.LabWorkspaceComponent
      )
  },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'paths' }
];
