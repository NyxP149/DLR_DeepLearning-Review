import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from './api-config';

export interface Dashboard {
  profile: { displayName: string; targetMonths: number; weekdayMinutes: number; weekendMinutes: number };
  totalLabs: number;
  completedLabs: number;
  progressPercent: number;
  averageScore: number;
  inProgressAttempts: number;
  pendingReviews: number;
  studyMinutes: number;
  currentStreak: number;
  bestStreak: number;
  xp: number;
  level: number;
  badges: { code: string; label: string; description: string; unlocked: boolean }[];
  nextLabCode: string | null;
  recentAttempts: { labCode: string; status: string; score: number | null; startedAt: string; completedAt: string | null }[];
}

@Injectable({ providedIn: 'root' })
export class DashboardApiService {
  private readonly http = inject(HttpClient);
  get() {
    return this.http.get<Dashboard>(`${API_BASE_URL}/dashboard`);
  }
}
