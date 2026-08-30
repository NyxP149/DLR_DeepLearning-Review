import { Injectable, signal } from '@angular/core';

export interface Ticket { readonly id: number; readonly title: string; readonly done: boolean; }

@Injectable({ providedIn: 'root' })
export class TicketService {
  readonly tickets = signal<readonly Ticket[]>([
    { id: 1, title: 'Mesurer le pipeline', done: false },
    { id: 2, title: 'Publier les preuves', done: true }
  ]);
}
