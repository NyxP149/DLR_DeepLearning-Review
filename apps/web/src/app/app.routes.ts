import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'labs/:code',
    loadComponent: () =>
      import('./features/lab-workspace/lab-workspace.component').then(
        (module) => module.LabWorkspaceComponent
      )
  },
  { path: '', pathMatch: 'full', redirectTo: 'labs/JAVA-01' },
  { path: '**', redirectTo: 'labs/JAVA-01' }
];

