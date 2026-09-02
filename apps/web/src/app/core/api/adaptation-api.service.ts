import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from './api-config';

export type AdaptationDecision = 'ACCEPT' | 'IGNORE' | 'POSTPONE' | 'REPLACE';

export interface AdaptationRecommendation {
  id: string;
  reason: string;
  targetedConcepts: string[];
  labCode: string;
  proposedActivity: string;
  difficulty: 'GUIDE' | 'RENFORCEMENT' | 'TRANSFERT' | 'CHALLENGE';
  expectedBenefit: string;
  factors: string[];
  status: 'PROPOSED' | 'ACCEPTED' | 'POSTPONED';
  expiresAt: string;
  createdAt: string;
  requiresConfirmation: boolean;
}

export interface AdaptationInsights {
  autonomyScore: number;
  hintDependencyPercent: number;
  transferScore: number;
  estimatedWeeksRemaining: number;
  factors: string[];
  disclaimer: string;
}

@Injectable({ providedIn: 'root' })
export class AdaptationApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/adaptation`;

  recommendation() {
    return this.http.get<AdaptationRecommendation>(`${this.url}/recommendation`);
  }

  insights() {
    return this.http.get<AdaptationInsights>(`${this.url}/insights`);
  }

  decide(id: string, decision: AdaptationDecision) {
    return this.http.post<AdaptationRecommendation>(`${this.url}/recommendations/${id}/decision`, { decision });
  }
}
