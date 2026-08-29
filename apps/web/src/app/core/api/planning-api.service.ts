import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface StudySession {
  date: string;
  plannedMinutes: number;
  actualMinutes: number;
  status: 'PLANNED' | 'COMPLETED' | 'SKIPPED';
  reward: string | null;
  completedAt: string | null;
}

export interface CalendarView {
  start: string;
  end: string;
  plannedMinutes: number;
  actualMinutes: number;
  sessions: StudySession[];
}

@Injectable({ providedIn: 'root' })
export class PlanningApiService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8081/api/calendar';
  get(days = 14) { return this.http.get<CalendarView>(this.url, { params: { days } }); }
  record(date: string, actualMinutes: number, reward: string) {
    return this.http.post<StudySession>(`${this.url}/${encodeURIComponent(date)}`, { actualMinutes, reward });
  }
}
