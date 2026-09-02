import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { LabContent } from '../../features/lab-workspace/lab.model';
import { API_BASE_URL } from './api-config';

export interface LabSummary {
  code: string;
  language: string;
  number: number;
  title: string;
  difficulty: string;
  threshold: number;
  activityType: 'LAB' | 'PROJECT' | 'CHALLENGE';
  prerequisites: string[];
}
export type LabProgressState = 'LOCKED' | 'AVAILABLE' | 'IN_PROGRESS' | 'ACTION_REQUIRED' | 'COMPLETED';
export interface PathProgress {
  pathCode: string; totalLabs: number; completedLabs: number; progressPercent: number; nextLabCode: string | null;
  labs: { code: string; title: string; activityType: 'LAB' | 'PROJECT' | 'CHALLENGE'; prerequisites: string[]; state: LabProgressState; bestScore: number | null }[];
}
export interface PathDescriptor {
  code: string; title: string; status: 'AVAILABLE' | 'BETA' | 'LOCKED' | 'PLANNED'; professionalObjectives: string[];
  prerequisites: string[]; keyConcepts: string[]; activityTypes: string[]; executionEnvironment: string;
  assessmentStrategy: string; project: string; challenge: string; portfolioSkills: string[]; expectedActivityCount: number;
}

@Injectable({ providedIn: 'root' })
export class LabApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = API_BASE_URL;

  getLab(code: string): Observable<LabContent> {
    return this.http.get<LabContent>(`${this.apiUrl}/labs/${encodeURIComponent(code)}`);
  }

  listLabs(): Observable<LabSummary[]> {
    return this.http.get<LabSummary[]>(`${this.apiUrl}/labs`);
  }
  listPaths(): Observable<PathDescriptor[]> { return this.http.get<PathDescriptor[]>(`${this.apiUrl}/paths/catalog`); }
  pathProgress(code: string): Observable<PathProgress> {
    return this.http.get<PathProgress>(`${this.apiUrl}/paths/${encodeURIComponent(code)}/progress`);
  }
}
