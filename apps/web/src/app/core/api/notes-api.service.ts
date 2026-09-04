import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api-config';

export interface PersonalNote {
  labCode: string;
  language: string;
  labTitle: string;
  content: string;
  updatedAt: string | null;
  completed: boolean;
}

export interface ReflectionAnalysis {
  labCode: string;
  questionId: string;
  content: string;
  updatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotesApiService {
  private readonly http = inject(HttpClient);

  list(): Observable<PersonalNote[]> {
    return this.http.get<PersonalNote[]>(`${API_BASE_URL}/notes`);
  }

  get(labCode: string): Observable<PersonalNote> {
    return this.http.get<PersonalNote>(`${API_BASE_URL}/labs/${encodeURIComponent(labCode)}/note`);
  }

  save(labCode: string, content: string): Observable<PersonalNote> {
    return this.http.put<PersonalNote>(
      `${API_BASE_URL}/labs/${encodeURIComponent(labCode)}/note`,
      { content }
    );
  }

  analyses(labCode: string): Observable<ReflectionAnalysis[]> {
    return this.http.get<ReflectionAnalysis[]>(
      `${API_BASE_URL}/labs/${encodeURIComponent(labCode)}/reflection-analyses`
    );
  }

  saveAnalysis(labCode: string, questionId: string, content: string): Observable<ReflectionAnalysis> {
    return this.http.put<ReflectionAnalysis>(
      `${API_BASE_URL}/labs/${encodeURIComponent(labCode)}/reflection-analyses/${encodeURIComponent(questionId)}`,
      { content }
    );
  }

  deleteAnalysis(labCode: string, questionId: string): Observable<void> {
    return this.http.delete<void>(
      `${API_BASE_URL}/labs/${encodeURIComponent(labCode)}/reflection-analyses/${encodeURIComponent(questionId)}`
    );
  }
}
