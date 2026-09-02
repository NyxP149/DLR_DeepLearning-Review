import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from './api-config';

export interface StudySession {
  date: string;
  plannedMinutes: number;
  actualMinutes: number;
  status: 'PLANNED' | 'COMPLETED' | 'SKIPPED';
  reward: string | null;
  completedAt: string | null;
}

export interface PlannedActivity {
  code: string;
  title: string;
  activityType: 'LAB' | 'PROJECT' | 'CHALLENGE';
  status: 'COMPLETED' | 'AVAILABLE' | 'IN_PROGRESS' | 'ACTION_REQUIRED' | 'WAITING_FOR_COMPLETION';
  effectiveDate: string | null;
  availableAfterLabCode: string | null;
  completedAt: string | null;
}

export interface CalendarView {
  start: string;
  end: string;
  plannedMinutes: number;
  actualMinutes: number;
  pathCode: string;
  activities: PlannedActivity[];
  sessions: StudySession[];
}

@Injectable({ providedIn: 'root' })
export class PlanningApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/calendar`;
  get(days = 14, path = 'JAVA') { return this.http.get<CalendarView>(this.url, { params: { days, path } }); }
  record(date: string, actualMinutes: number, reward: string) {
    return this.http.post<StudySession>(`${this.url}/${encodeURIComponent(date)}`, { actualMinutes, reward });
  }
}
