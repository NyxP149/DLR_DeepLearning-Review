import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface ReviewItem {
  id: string;
  attemptId: string;
  labCode: string;
  dueAt: string;
  reason: string;
  status: string;
  stage: number;
}

@Injectable({ providedIn: 'root' })
export class ReviewApiService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8081/api/reviews';
  pending() { return this.http.get<ReviewItem[]>(this.url); }
  complete(id: string, successful: boolean) {
    return this.http.post(`${this.url}/${encodeURIComponent(id)}/complete`, { successful });
  }
}
