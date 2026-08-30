import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { TicketService } from './ticket.service';

@Component({
  selector: 'dlr-dashboard', standalone: true, changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html', styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  readonly store = inject(TicketService);
  readonly completed = computed(() => this.store.tickets().filter(ticket => ticket.done).length);
}
