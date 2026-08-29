import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Attempt {
  id: string;
  labCode: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'COMPLETED_BELOW_THRESHOLD';
}

export interface SubmissionResponse {
  id: string;
  attemptId: string;
  language: string;
  origin: 'EDITOR' | 'PASTE' | 'IMPORT';
  createdAt: string;
}

export interface ExecutionResult {
  id: string;
  submissionId: string;
  status: 'SUCCESS' | 'COMPILATION_ERROR' | 'RUNTIME_ERROR' | 'TIMEOUT' | 'RUNNER_ERROR';
  exitCode: number | null;
  standardOutput: string;
  errorOutput: string;
  durationMs: number;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ExecutionApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api';

  startAttempt(labCode: string): Observable<Attempt> {
    return this.http.post<Attempt>(
      `${this.apiUrl}/labs/${encodeURIComponent(labCode)}/attempts`,
      {}
    );
  }

  submit(attemptId: string, sourceCode: string): Observable<SubmissionResponse> {
    return this.http.post<SubmissionResponse>(
      `${this.apiUrl}/attempts/${encodeURIComponent(attemptId)}/submissions`,
      { language: 'JAVA', sourceCode, origin: 'EDITOR' }
    );
  }

  run(submissionId: string): Observable<ExecutionResult> {
    return this.http.post<ExecutionResult>(
      `${this.apiUrl}/submissions/${encodeURIComponent(submissionId)}/run`,
      {}
    );
  }
}
