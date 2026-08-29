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
}
