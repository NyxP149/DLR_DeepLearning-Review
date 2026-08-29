import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface Dashboard {
  profile: { displayName: string; targetMonths: number; weekdayMinutes: number; weekendMinutes: number };
  totalLabs: number;
  completedLabs: number;
  progressPercent: number;
  averageScore: number;
  inProgressAttempts: number;
  pendingReviews: number;
  xp: number;
  level: number;
  nextLabCode: string | null;
  recentAttempts: { labCode: string; status: string; score: number | null; startedAt: string; completedAt: string | null }[];
}

@Injectable({ providedIn: 'root' })
export class DashboardApiService {
  private readonly http = inject(HttpClient);
  get() {
    return this.http.get<Dashboard>('http://localhost:8081/api/dashboard');
  }
}
