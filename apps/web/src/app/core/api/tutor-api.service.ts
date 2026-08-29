import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface TutorStatus { available: boolean; selectedModel: string; installedModels: string[]; }
export interface TutorResponse { purpose: string; model: string; content: string; }

@Injectable({ providedIn: 'root' })
export class TutorApiService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8081/api/tutor';
  status() { return this.http.get<TutorStatus>(`${this.url}/status`); }
  explain(labCode: string, conceptCode: string) {
    return this.http.post<TutorResponse>(`${this.url}/explain`, { labCode, conceptCode, question: 'Explique-moi ce concept progressivement avec un exemple concret.' });
  }
  hint(labCode: string, sourceCode: string, level: number) {
    return this.http.post<TutorResponse>(`${this.url}/hint`, { labCode, sourceCode, level });
  }
}
