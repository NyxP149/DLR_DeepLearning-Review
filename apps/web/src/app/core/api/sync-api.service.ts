import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface SyncDevice {
  id: string;
  name: string;
  status: string;
  pairedAt: string;
  lastSeenAt: string;
}

interface PairingResult {
  deviceId: string;
  deviceName: string;
  token: string;
  pairedAt: string;
}

@Injectable({ providedIn: 'root' })
export class SyncApiService {
  private readonly http = inject(HttpClient);
  private readonly tokenKey = 'dlr.sync.device-token';
  private readonly deviceKey = 'dlr.sync.device-id';
  private readonly serverKey = 'dlr.sync.server-url';

  serverUrl(): string {
    return localStorage.getItem(this.serverKey) ?? 'http://localhost:8081';
  }

  configureServer(value: string): void {
    localStorage.setItem(this.serverKey, value.replace(/\/+$/, ''));
  }

  isPaired(): boolean { return this.token() !== null; }
  currentDeviceId(): string | null { return localStorage.getItem(this.deviceKey); }

  pair(name: string, pairingCode: string) {
    const headers = pairingCode ? new HttpHeaders({ 'X-DLR-Pairing-Code': pairingCode }) : undefined;
    return this.http.post<PairingResult>(`${this.serverUrl()}/api/sync/devices`, { name }, { headers });
  }

  remember(pairing: PairingResult): void {
    localStorage.setItem(this.tokenKey, pairing.token);
    localStorage.setItem(this.deviceKey, pairing.deviceId);
  }

  devices() {
    return this.http.get<SyncDevice[]>(`${this.serverUrl()}/api/sync/devices`, { headers: this.authHeaders() });
  }

  revoke(deviceId: string) {
    return this.http.delete<void>(`${this.serverUrl()}/api/sync/devices/${deviceId}`, { headers: this.authHeaders() });
  }

  forget(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.deviceKey);
  }

  private token(): string | null { return localStorage.getItem(this.tokenKey); }
  private authHeaders(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.token() ?? ''}` });
  }
}
