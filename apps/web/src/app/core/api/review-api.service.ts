import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from './api-config';

export interface ReviewItem {
  id: string;
  attemptId: string;
  labCode: string;
  dueAt: string;
  reason: string;
  status: string;
  stage: number;
  belowThreshold: boolean;
  bestScore: number | null;
}

export interface ReviewQuestion {
  reviewId: string;
  labCode: string;
  questionId: string;
  type: 'SINGLE_CHOICE' | 'FREE_TEXT';
  prompt: string;
  choices: string[];
  stage: number;
}

export interface ReviewAnswerResult {
  outcome: 'CORRECT' | 'PARTIAL' | 'WRONG';
  score: number;
  feedback: string;
  explanation: string | null;
  modelAnswer: string | null;
  correctChoice: number | null;
  missingIdeas: string[];
  calibration: string;
  nextReview: ReviewItem | null;
}

@Injectable({ providedIn: 'root' })
export class ReviewApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/reviews`;
  pending() { return this.http.get<ReviewItem[]>(this.url); }
  question(id: string) { return this.http.get<ReviewQuestion>(`${this.url}/${encodeURIComponent(id)}/question`); }
  answer(id: string, answer: { questionId: string; selectedChoice?: number; answerText?: string; confidence: number }) {
    return this.http.post<ReviewAnswerResult>(`${this.url}/${encodeURIComponent(id)}/answer`, answer);
  }
  complete(id: string, successful: boolean) {
    return this.http.post(`${this.url}/${encodeURIComponent(id)}/complete`, { successful });
  }
}
