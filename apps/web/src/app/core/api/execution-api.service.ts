import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Attempt {
  id: string;
  labCode: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'COMPLETED_BELOW_THRESHOLD';
  score: number | null;
  continuedBelowThreshold: boolean;
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
  status: 'SUCCESS' | 'TESTS_FAILED' | 'COMPILATION_ERROR' | 'RUNTIME_ERROR' | 'TIMEOUT' | 'RUNNER_ERROR';
  exitCode: number | null;
  standardOutput: string;
  errorOutput: string;
  durationMs: number;
  createdAt: string;
}

export interface QuizAnswerResponse {
  questionId: string;
  score: number;
  feedback: string;
}

export interface CompletionResult {
  attempt: Attempt;
  breakdown: {
    tests: number;
    quiz: number;
    practice: number;
    connections: number;
    selfAssessment: number;
    version: string;
  };
  threshold: number;
  reviewScheduled: boolean;
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

  answerQuiz(
    attemptId: string,
    questionId: string,
    answer: { selectedChoice?: number; answerText?: string }
  ): Observable<QuizAnswerResponse> {
    return this.http.put<QuizAnswerResponse>(
      `${this.apiUrl}/attempts/${encodeURIComponent(attemptId)}/quiz/${encodeURIComponent(questionId)}`,
      answer
    );
  }

  saveChecklist(attemptId: string, completed: boolean[]): Observable<void> {
    return this.http.put<void>(
      `${this.apiUrl}/attempts/${encodeURIComponent(attemptId)}/checklist`,
      { completed }
    );
  }

  complete(attemptId: string): Observable<CompletionResult> {
    return this.http.post<CompletionResult>(
      `${this.apiUrl}/attempts/${encodeURIComponent(attemptId)}/complete`,
      {}
    );
  }

  continueBelowThreshold(attemptId: string): Observable<Attempt> {
    return this.http.post<Attempt>(
      `${this.apiUrl}/attempts/${encodeURIComponent(attemptId)}/continue`,
      {}
    );
  }
}
