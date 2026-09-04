import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

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

export interface ExecutionAvailability {
  available: boolean;
  mode: 'LOCAL_DOCKER' | 'DISABLED' | 'UNAVAILABLE';
  message: string;
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

export interface AttemptWorkspace {
  attempt: Attempt;
  sourceCode: string | null;
  sourceOrigin: 'EDITOR' | 'PASTE' | 'IMPORT' | null;
  quizAnswers: { questionId: string; selectedChoice: number | null; answerText: string | null; feedback: string }[];
  checklist: boolean[];
}

export interface LabResetResult {
  labCode: string;
  attemptsDeleted: number;
  submissionsDeleted: number;
  executionsDeleted: number;
  reviewsDeleted: number;
}

@Injectable({ providedIn: 'root' })
export class ExecutionApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = API_BASE_URL;

  status(): Observable<ExecutionAvailability> {
    return this.http.get<ExecutionAvailability>(`${this.apiUrl}/execution/status`);
  }

  startAttempt(labCode: string): Observable<Attempt> {
    return this.http.post<Attempt>(
      `${this.apiUrl}/labs/${encodeURIComponent(labCode)}/attempts`,
      {}
    );
  }

  currentWorkspace(labCode: string): Observable<AttemptWorkspace | null> {
    return this.http.get<AttemptWorkspace | null>(
      `${this.apiUrl}/labs/${encodeURIComponent(labCode)}/attempts/current`
    );
  }

  submit(
    attemptId: string,
    sourceCode: string,
    language: string,
    origin: 'EDITOR' | 'PASTE' | 'IMPORT' = 'EDITOR'
  ): Observable<SubmissionResponse> {
    return this.http.post<SubmissionResponse>(
      `${this.apiUrl}/attempts/${encodeURIComponent(attemptId)}/submissions`,
      { language, sourceCode, origin }
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

  deleteQuizAnswer(attemptId: string, questionId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/attempts/${encodeURIComponent(attemptId)}/quiz/${encodeURIComponent(questionId)}`
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

  resetLab(labCode: string): Observable<LabResetResult> {
    return this.http.delete<LabResetResult>(
      `${this.apiUrl}/labs/${encodeURIComponent(labCode)}/progress`
    );
  }
}
