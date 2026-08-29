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
    path: 'labs/:code',
    loadComponent: () =>
      import('./features/lab-workspace/lab-workspace.component').then(
        (module) => module.LabWorkspaceComponent
      )
  },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'paths' }
];
