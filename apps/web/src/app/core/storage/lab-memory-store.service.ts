import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LabMemoryStoreService {
  loadNote(labCode: string): string {
    return localStorage.getItem(`dlr:note:${labCode}`) ?? '';
  }

  noteUpdatedAt(labCode: string): string | null {
    return localStorage.getItem(`dlr:note-updated:${labCode}`);
  }

  saveNote(labCode: string, content: string, updatedAt = new Date().toISOString()): void {
    localStorage.setItem(`dlr:note:${labCode}`, content);
    localStorage.setItem(`dlr:note-updated:${labCode}`, updatedAt);
  }

  loadReflections(labCode: string): Record<string, number | string> {
    try {
      const stored = JSON.parse(localStorage.getItem(`dlr:reflections:${labCode}`) ?? '{}');
      return stored !== null && typeof stored === 'object' ? stored : {};
    } catch {
      return {};
    }
  }

  saveReflection(labCode: string, questionId: string, value: number | string): void {
    const reflections = this.loadReflections(labCode);
    reflections[questionId] = value;
    localStorage.setItem(`dlr:reflections:${labCode}`, JSON.stringify(reflections));
  }

  removeReflections(labCode: string): void {
    localStorage.removeItem(`dlr:reflections:${labCode}`);
  }

  loadAnalyses(labCode: string): Record<string, string> {
    try {
      const stored = JSON.parse(localStorage.getItem(`dlr:analyses:${labCode}`) ?? '{}');
      return stored !== null && typeof stored === 'object' ? stored : {};
    } catch {
      return {};
    }
  }

  saveAnalysis(labCode: string, questionId: string, content: string): void {
    const analyses = this.loadAnalyses(labCode);
    analyses[questionId] = content;
    localStorage.setItem(`dlr:analyses:${labCode}`, JSON.stringify(analyses));
  }

  deleteAnalysis(labCode: string, questionId: string): void {
    const analyses = this.loadAnalyses(labCode);
    delete analyses[questionId];
    localStorage.setItem(`dlr:analyses:${labCode}`, JSON.stringify(analyses));
  }
}
