import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { Profile, ProfileApiService } from '../../core/api/profile-api.service';
import { TutorApiService, TutorStatus } from '../../core/api/tutor-api.service';
import { SyncApiService, SyncDevice } from '../../core/api/sync-api.service';
import { ThemeId, ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'dlr-settings',
  imports: [],
  template: `
    <header><p>Configuration locale</p><h1>Paramètres</h1><span>Ton rythme et le professeur local restent sous ton contrôle.</span></header>
    @if (error()) { <p class="state error" role="alert">{{ error() }}</p> }
    <div class="grid">
      <section class="theme-panel">
        <div class="section-heading"><div><p>Apparence</p><h2>Thème de l’interface</h2></div><span>{{ themes.current() === 'monochrome' ? 'Contraste maximal' : 'Palette professionnelle' }}</span></div>
        <p class="theme-intro">Chaque palette adapte les surfaces, boutons, champs, zones de texte, états et l’éditeur afin que les libellés restent lisibles.</p>
        <div class="theme-grid" role="radiogroup" aria-label="Choisir le thème de l’interface">
          @for (theme of themes.themes; track theme.id) {
            <button type="button" class="theme-card" role="radio" [attr.aria-checked]="themes.current() === theme.id" [class.selected]="themes.current() === theme.id" (click)="selectTheme(theme.id)">
              <span class="theme-preview" [attr.data-preview]="theme.id">
                @for (color of theme.colors; track color) { <i [style.background]="color"></i> }
              </span>
              <strong>{{ theme.name }}</strong>
              <small>{{ theme.description }}</small>
              <span class="theme-mode">{{ theme.mode === 'dark' ? 'Sombre' : 'Clair' }}{{ themes.current() === theme.id ? ' · Actif' : '' }}</span>
            </button>
          }
        </div>
      </section>
      <form (submit)="submit($event)">
        <h2>Profil d’apprentissage</h2>
        <label>Nom affiché <input name="displayName" required maxlength="80" [value]="profile.displayName" (input)="setText('displayName', $event)"></label>
        <label>Durée cible
          <select name="targetMonths" [value]="profile.targetMonths" (change)="setNumber('targetMonths', $event)"><option value="4">4 mois · recommandé</option><option value="3">3 mois · accéléré</option></select>
        </label>
        <label>Minutes du lundi au vendredi <input name="weekdayMinutes" type="number" min="15" max="240" [value]="profile.weekdayMinutes" (input)="setNumber('weekdayMinutes', $event)"></label>
        <label>Minutes le week-end <input name="weekendMinutes" type="number" min="15" max="240" [value]="profile.weekendMinutes" (input)="setNumber('weekendMinutes', $event)"></label>
        <button type="submit" [disabled]="saving()">{{ saving() ? 'Enregistrement…' : 'Enregistrer le profil' }}</button>
        @if (saved()) { <span class="saved">Profil enregistré.</span> }
      </form>
      <section>
        <h2>Professeur Ollama</h2>
        @if (tutor(); as status) {
          <p class="ollama"><span [class.online]="status.available"></span>{{ status.available ? 'Disponible' : 'Mode dégradé' }}</p>
          <dl><div><dt>Modèle sélectionné</dt><dd>{{ status.selectedModel }}</dd></div><div><dt>Modèles installés</dt><dd>{{ status.installedModels.join(', ') || 'Aucun détecté' }}</dd></div></dl>
          <small>Le modèle se configure dans <code>.env</code>. Les échanges restent sur cette machine.</small>
        } @else { <p class="muted">Statut Ollama indisponible. Le reste de DLR continue de fonctionner.</p> }
      </section>
      <section class="sync-panel">
        <div class="section-heading"><div><p>Synchronisation personnelle</p><h2>Appareils V2</h2></div><span [class.online]="paired()">{{ paired() ? 'Appairé' : 'Local uniquement' }}</span></div>
        <div class="pairing-form">
          <label>Serveur DLR <input [value]="syncServer" (input)="syncServer = inputValue($event)" placeholder="http://localhost:8081"></label>
          <label>Nom de cet appareil <input [value]="deviceName" (input)="deviceName = inputValue($event)" maxlength="80"></label>
          <label>Code d’appairage <input type="password" [value]="pairingCode" (input)="pairingCode = inputValue($event)" autocomplete="one-time-code" placeholder="Optionnel sur le PC principal"></label>
          <button type="button" [disabled]="syncBusy() || paired()" (click)="pairDevice()">{{ syncBusy() ? 'Appairage…' : 'Appairer cet appareil' }}</button>
        </div>
        <small>Le jeton est conservé uniquement dans ce navigateur. Sans code configuré côté serveur, l’appairage est limité au PC principal.</small>
        @if (syncMessage()) { <p class="sync-message" aria-live="polite">{{ syncMessage() }}</p> }
        @if (devices().length) {
          <div class="device-list">
            @for (device of devices(); track device.id) {
              <div><span><strong>{{ device.name }}</strong><small>Dernière activité : {{ device.lastSeenAt }}</small></span><button type="button" class="danger" (click)="revokeDevice(device.id)">Révoquer</button></div>
            }
          </div>
        }
      </section>
    </div>
  `,
  styles: [`
    :host{display:block}header{margin-bottom:1.5rem}header p,.section-heading p{color:var(--accent);font-size:.78rem;font-weight:750;letter-spacing:.09em;text-transform:uppercase}header h1{font-size:clamp(2rem,4vw,3rem);margin:.2rem 0}header span,label,small,.muted,dt{color:var(--text-muted)}.grid{display:grid;gap:1rem;grid-template-columns:1fr 1fr}.grid>form,.grid>section{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1.2rem}.theme-panel,.sync-panel{grid-column:1/-1}.theme-intro{color:var(--text-muted);font-size:.85rem;margin:-.35rem 0 1rem}.theme-grid{display:grid;gap:.7rem;grid-template-columns:repeat(3,1fr)}.theme-card{background:var(--surface-raised);border:1px solid var(--border);color:var(--text);display:grid;gap:.45rem;padding:.8rem;text-align:left}.theme-card:hover{border-color:var(--accent)}.theme-card.selected{border-color:var(--accent);box-shadow:inset 0 0 0 1px var(--accent)}.theme-card small{line-height:1.4;min-height:2rem}.theme-preview{border:1px solid color-mix(in srgb,var(--border) 65%,transparent);border-radius:.55rem;display:grid;grid-template-columns:1.5fr 1fr 1fr;height:3.2rem;overflow:hidden}.theme-preview i{display:block}.theme-mode{color:var(--accent)!important;font-size:.68rem;font-weight:800;text-transform:uppercase}h2{font-size:1.1rem;margin:0 0 1rem}form{display:grid;gap:.85rem}label{display:grid;font-size:.78rem;gap:.3rem}input,select{background:var(--input-background);border:1px solid var(--border);border-radius:.55rem;color:var(--text);font:inherit;padding:.65rem}button{background:var(--accent);border:0;border-radius:.55rem;color:var(--on-accent);cursor:pointer;font:inherit;font-weight:750;padding:.7rem}button:disabled{cursor:not-allowed;opacity:.55}.saved{color:var(--success);font-size:.78rem}.ollama{align-items:center;display:flex;font-weight:750;gap:.5rem}.ollama span{background:var(--danger);border-radius:50%;height:.7rem;width:.7rem}.ollama span.online{background:var(--success)}dl div{border-top:1px solid var(--border);display:grid;gap:.25rem;padding:.8rem 0}dt{font-size:.72rem}dd{margin:0}.state{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1rem}.error{color:var(--danger)}.section-heading{align-items:center;display:flex;justify-content:space-between}.section-heading p{margin:0}.section-heading h2{margin:.25rem 0 1rem}.section-heading>span{background:var(--surface-raised);border:1px solid var(--border);border-radius:99px;color:var(--text-muted);font-size:.75rem;padding:.45rem .7rem}.section-heading>span.online{color:var(--success)}.pairing-form{align-items:end;display:grid;gap:.75rem;grid-template-columns:1.2fr 1fr 1fr auto}.device-list{display:grid;gap:.5rem;margin-top:1rem}.device-list>div{align-items:center;background:var(--surface-raised);border:1px solid var(--border);border-radius:.65rem;display:flex;justify-content:space-between;padding:.75rem}.device-list span{display:grid;gap:.2rem}.danger{background:transparent;border:1px solid var(--danger);color:var(--danger);font-size:.75rem}.sync-message{color:var(--success);font-size:.82rem}@media(max-width:900px){.pairing-form,.theme-grid{grid-template-columns:1fr 1fr}}@media(max-width:760px){.grid,.pairing-form,.theme-grid{grid-template-columns:1fr}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsComponent {
  private readonly profiles = inject(ProfileApiService);
  private readonly tutors = inject(TutorApiService);
  private readonly sync = inject(SyncApiService);
  readonly themes = inject(ThemeService);
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly tutor = signal<TutorStatus | null>(null);
  readonly devices = signal<SyncDevice[]>([]);
  readonly paired = signal(this.sync.isPaired());
  readonly syncBusy = signal(false);
  readonly syncMessage = signal<string | null>(null);
  syncServer = this.sync.serverUrl();
  deviceName = typeof navigator === 'undefined' ? 'Appareil DLR' : `DLR · ${navigator.platform || 'navigateur'}`;
  pairingCode = '';
  profile: Profile = { displayName: '', targetMonths: 4, weekdayMinutes: 90, weekendMinutes: 60 };

  constructor() { void this.load(); }

  submit(event: Event): void { event.preventDefault(); void this.save(); }
  setText(field: 'displayName', event: Event): void { this.profile = { ...this.profile, [field]: (event.target as HTMLInputElement).value }; }
  setNumber(field: 'targetMonths' | 'weekdayMinutes' | 'weekendMinutes', event: Event): void {
    this.profile = { ...this.profile, [field]: Number((event.target as HTMLInputElement).value) };
  }
  inputValue(event: Event): string { return (event.target as HTMLInputElement).value; }
  selectTheme(theme: ThemeId): void { this.themes.select(theme); }

  async pairDevice(): Promise<void> {
    if (!this.deviceName.trim() || !this.syncServer.trim()) return;
    this.syncBusy.set(true); this.syncMessage.set(null);
    try {
      this.sync.configureServer(this.syncServer.trim());
      const pairing = await firstValueFrom(this.sync.pair(this.deviceName.trim(), this.pairingCode));
      this.sync.remember(pairing); this.paired.set(true); this.pairingCode = '';
      await this.loadDevices(); this.syncMessage.set('Appareil appairé. Le jeton privé ne sera plus affiché.');
    } catch { this.syncMessage.set('Appairage refusé. Vérifie le serveur et le code d’appairage.'); }
    finally { this.syncBusy.set(false); }
  }

  async revokeDevice(deviceId: string): Promise<void> {
    this.syncBusy.set(true); this.syncMessage.set(null);
    try {
      await firstValueFrom(this.sync.revoke(deviceId));
      if (deviceId === this.sync.currentDeviceId()) { this.sync.forget(); this.paired.set(false); this.devices.set([]); }
      else await this.loadDevices();
      this.syncMessage.set('Appareil révoqué. Son ancien jeton est désormais inutilisable.');
    } catch { this.syncMessage.set('La révocation a échoué. Recharge la liste des appareils.'); }
    finally { this.syncBusy.set(false); }
  }

  async save(): Promise<void> {
    this.saving.set(true); this.saved.set(false);
    try { this.profile = await firstValueFrom(this.profiles.update(this.profile)); this.saved.set(true); this.error.set(null); }
    catch { this.error.set('Vérifie le nom, la durée cible et les minutes indiquées.'); }
    finally { this.saving.set(false); }
  }

  private async load(): Promise<void> {
    try { this.profile = await firstValueFrom(this.profiles.get()); }
    catch { this.error.set('Impossible de charger le profil local.'); }
    try { this.tutor.set(await firstValueFrom(this.tutors.status())); } catch { this.tutor.set(null); }
    if (this.paired()) await this.loadDevices();
  }

  private async loadDevices(): Promise<void> {
    try { this.devices.set(await firstValueFrom(this.sync.devices())); }
    catch { this.sync.forget(); this.paired.set(false); this.devices.set([]); }
  }
}
