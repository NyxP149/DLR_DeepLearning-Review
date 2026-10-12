import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from './api-config';

export interface PortfolioProject {
  id: string;
  slug: string;
  title: string;
  summary: string;
  labCodes: string[];
  status: 'PRIVATE';
  decisions: string[];
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioDraft {
  title: string;
  summary: string;
  labCodes: string[];
  decisions: string;
  updatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class PortfolioApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/portfolio/projects`;
  private readonly draftUrl = `${API_BASE_URL}/portfolio/draft`;
  draft() { return this.http.get<PortfolioDraft | null>(this.draftUrl); }
  saveDraft(draft: Omit<PortfolioDraft, 'updatedAt'>) { return this.http.put<PortfolioDraft>(this.draftUrl, draft); }
  list() { return this.http.get<PortfolioProject[]>(this.url); }
  create(title: string, summary: string, labCodes: string[], decisions: string[]) {
    return this.http.post<PortfolioProject>(this.url, { title, summary, labCodes, decisions });
  }
  readme(id: string) { return this.http.get(`${this.url}/${id}/readme`, { responseType: 'text' }); }
  export(id: string) { return this.http.get(`${this.url}/${id}/export`, { responseType: 'blob' }); }
}
