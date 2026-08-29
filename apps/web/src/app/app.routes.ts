import { Routes } from '@angular/router';

export const routes: Routes = [
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
  { path: '', pathMatch: 'full', redirectTo: 'paths' },
  { path: '**', redirectTo: 'paths' }
];
