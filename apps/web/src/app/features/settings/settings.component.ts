import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { Profile, ProfileApiService } from '../../core/api/profile-api.service';
import { TutorApiService, TutorStatus } from '../../core/api/tutor-api.service';

@Component({
  selector: 'dlr-settings',
  imports: [],
  template: `
    <header><p>Configuration locale</p><h1>Paramètres</h1><span>Ton rythme et le professeur local restent sous ton contrôle.</span></header>
    @if (error()) { <p class="state error" role="alert">{{ error() }}</p> }
    <div class="grid">
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
    </div>
  `,
  styles: [`
    :host{display:block}header{margin-bottom:1.5rem}header p{color:var(--accent);font-size:.78rem;font-weight:750;letter-spacing:.09em;text-transform:uppercase}header h1{font-size:clamp(2rem,4vw,3rem);margin:.2rem 0}header span,label,small,.muted,dt{color:var(--text-muted)}.grid{display:grid;gap:1rem;grid-template-columns:1fr 1fr}.grid>form,.grid>section{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1.2rem}h2{font-size:1.1rem;margin:0 0 1rem}form{display:grid;gap:.85rem}label{display:grid;font-size:.78rem;gap:.3rem}input,select{background:var(--background);border:1px solid var(--border);border-radius:.55rem;color:var(--text);font:inherit;padding:.65rem}button{background:var(--accent);border:0;border-radius:.55rem;color:white;cursor:pointer;font:inherit;font-weight:750;padding:.7rem}.saved{color:var(--success);font-size:.78rem}.ollama{align-items:center;display:flex;font-weight:750;gap:.5rem}.ollama span{background:var(--danger);border-radius:50%;height:.7rem;width:.7rem}.ollama span.online{background:var(--success)}dl div{border-top:1px solid var(--border);display:grid;gap:.25rem;padding:.8rem 0}dt{font-size:.72rem}dd{margin:0}.state{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1rem}.error{color:var(--danger)}@media(max-width:760px){.grid{grid-template-columns:1fr}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsComponent {
  private readonly profiles = inject(ProfileApiService);
  private readonly tutors = inject(TutorApiService);
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly tutor = signal<TutorStatus | null>(null);
  profile: Profile = { displayName: '', targetMonths: 4, weekdayMinutes: 90, weekendMinutes: 60 };

  constructor() { void this.load(); }

  submit(event: Event): void { event.preventDefault(); void this.save(); }
  setText(field: 'displayName', event: Event): void { this.profile = { ...this.profile, [field]: (event.target as HTMLInputElement).value }; }
  setNumber(field: 'targetMonths' | 'weekdayMinutes' | 'weekendMinutes', event: Event): void {
    this.profile = { ...this.profile, [field]: Number((event.target as HTMLInputElement).value) };
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
  }
}
