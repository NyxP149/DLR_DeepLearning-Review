import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { LabContent } from '../../features/lab-workspace/lab.model';

export interface LabSummary {
  code: string;
  language: string;
  number: number;
  title: string;
  difficulty: string;
  threshold: number;
}
export interface PathDescriptor {
  code: string; title: string; status: 'AVAILABLE' | 'BETA' | 'LOCKED' | 'PLANNED'; professionalObjectives: string[];
  prerequisites: string[]; keyConcepts: string[]; activityTypes: string[]; executionEnvironment: string;
  assessmentStrategy: string; project: string; challenge: string; portfolioSkills: string[]; expectedActivityCount: number;
}

@Injectable({ providedIn: 'root' })
export class LabApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api';

  getLab(code: string): Observable<LabContent> {
    return this.http.get<LabContent>(`${this.apiUrl}/labs/${encodeURIComponent(code)}`);
  }

  listLabs(): Observable<LabSummary[]> {
    return this.http.get<LabSummary[]>(`${this.apiUrl}/labs`);
  }
  listPaths(): Observable<PathDescriptor[]> { return this.http.get<PathDescriptor[]>(`${this.apiUrl}/paths/catalog`); }
}
