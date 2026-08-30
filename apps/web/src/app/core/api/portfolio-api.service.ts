import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

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

@Injectable({ providedIn: 'root' })
export class PortfolioApiService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8081/api/portfolio/projects';
  list() { return this.http.get<PortfolioProject[]>(this.url); }
  create(title: string, summary: string, labCodes: string[], decisions: string[]) {
    return this.http.post<PortfolioProject>(this.url, { title, summary, labCodes, decisions });
  }
  readme(id: string) { return this.http.get(`${this.url}/${id}/readme`, { responseType: 'text' }); }
  export(id: string) { return this.http.get(`${this.url}/${id}/export`, { responseType: 'blob' }); }
}
