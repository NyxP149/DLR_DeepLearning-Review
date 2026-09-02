import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from './api-config';

export interface Profile {
  displayName: string;
  targetMonths: number;
  weekdayMinutes: number;
  weekendMinutes: number;
}

@Injectable({ providedIn: 'root' })
export class ProfileApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/profile`;
  get() { return this.http.get<Profile>(this.url); }
  update(profile: Profile) { return this.http.put<Profile>(this.url, profile); }
}
