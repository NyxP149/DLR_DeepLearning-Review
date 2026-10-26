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

export interface CalibrationLevel {
  confidence: 1 | 2 | 3;
  total: number;
  correct: number;
  partial: number;
  wrong: number;
  correctPercent: number;
}

export interface CalibrationSource {
  code: 'QUIZ' | 'REVIEW';
  total: number;
  correctPercent: number;
}

export interface CalibrationBlindSpot {
  labCode: string;
  questionId: string;
  prompt: string;
  outcome: 'PARTIAL' | 'WRONG';
  source: 'QUIZ' | 'REVIEW';
  answeredAt: string;
}

export interface CalibrationPeriod {
  weekStart: string;
  total: number;
  confidencePercent: number;
  accuracyPercent: number;
  gapPoints: number;
  sureTotal: number;
  overconfidencePercent: number;
  reliable: boolean;
}

export interface CalibrationTrend {
  verdict: 'INSUFFICIENT_DATA' | 'IMPROVING' | 'STABLE' | 'WORSENING';
  message: string;
  periods: CalibrationPeriod[];
}

export interface Calibration {
  totalAnswers: number;
  levels: CalibrationLevel[];
  sources: CalibrationSource[];
  verdict: 'INSUFFICIENT_DATA' | 'OVERCONFIDENT' | 'UNDERCONFIDENT' | 'CALIBRATED';
  message: string;
  overconfidencePercent: number;
  underconfidencePercent: number;
  blindSpots: CalibrationBlindSpot[];
  trend: CalibrationTrend;
}

export interface RetentionBucket {
  label: string;
  fromDays: number;
  toDays: number | null;
  total: number;
  correct: number;
  partial: number;
  wrong: number;
  correctPercent: number;
  initialSamples: number;
  initialPercent: number;
}

export interface RetentionForgotten {
  labCode: string;
  questionId: string;
  prompt: string;
  outcome: 'PARTIAL' | 'WRONG';
  delayDays: number;
  answeredAt: string;
}

export interface Retention {
  totalRecalls: number;
  buckets: RetentionBucket[];
  verdict: 'INSUFFICIENT_DATA' | 'TOO_EARLY' | 'DURABLE' | 'MIXED' | 'FRAGILE';
  message: string;
  retainedLabs: { labCode: string; delayDays: number }[];
  forgotten: RetentionForgotten[];
}

export interface PretestGainLab {
  labCode: string;
  title: string;
  total: number;
  correctAtStart: number;
  correctAtEnd: number;
  gainPoints: number;
}

export interface PretestGainSummary {
  pretestsAnswered: number;
  pretestsSkipped: number;
  labsCompared: number;
  questions: number;
  startCorrectPercent: number;
  endCorrectPercent: number;
  gainPoints: number;
  learned: number;
  lost: number;
  unknownAtStartPercent: number;
  verdict: 'INSUFFICIENT_DATA' | 'ALREADY_KNOWN' | 'STRONG_GAIN' | 'MODERATE_GAIN' | 'LOW_GAIN';
  message: string;
  labs: PretestGainLab[];
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

  pretestGain() {
    return this.http.get<PretestGainSummary>(`${this.url}/pretest-gain`);
  }

  calibration() {
    return this.http.get<Calibration>(`${this.url}/calibration`);
  }

  retention() {
    return this.http.get<Retention>(`${this.url}/retention`);
  }

  decide(id: string, decision: AdaptationDecision) {
    return this.http.post<AdaptationRecommendation>(`${this.url}/recommendations/${id}/decision`, { decision });
  }
}
