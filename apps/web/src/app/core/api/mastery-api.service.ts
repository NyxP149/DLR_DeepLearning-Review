import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export type MasteryStatus = 'NOT_STARTED' | 'TO_REVIEW' | 'CONSOLIDATING' | 'MASTERED';

export interface ConceptMastery {
  code: string;
  name: string;
  labCode: string;
  labNumber: number;
  labTitle: string;
  definition: string;
  whyExists: string;
  whyImportant: string;
  minimalExample: string;
  commonMistake: string;
  masteryQuestion: string;
  masteryProof: string;
  status: MasteryStatus;
  score: number | null;
  completedReviewStage: number;
}

@Injectable({ providedIn: 'root' })
export class MasteryApiService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8081/api/mastery/concepts';

  listConcepts() {
    return this.http.get<ConceptMastery[]>(this.url);
  }
}
