import { Injectable, signal } from '@angular/core';

export type ThemeId = 'obsidian' | 'monochrome' | 'boreal' | 'emerald' | 'royal' | 'solar';

export interface ThemeOption {
  id: ThemeId;
  name: string;
  description: string;
  colors: readonly [string, string, string];
  mode: 'dark' | 'light';
}

export const DLR_THEMES: readonly ThemeOption[] = [
  { id: 'obsidian', name: 'Obsidienne', description: 'Noir profond, violet électrique et menthe.', colors: ['#080d1b', '#8b80ff', '#48d7b0'], mode: 'dark' },
  { id: 'monochrome', name: 'Noir & Blanc', description: 'Contraste éditorial, net et intemporel.', colors: ['#ffffff', '#111111', '#d9d9d9'], mode: 'light' },
  { id: 'boreal', name: 'Océan Boréal', description: 'Bleu pétrole, cyan glacé et corail.', colors: ['#061c27', '#22c7d6', '#ff8066'], mode: 'dark' },
  { id: 'emerald', name: 'Forêt Émeraude', description: 'Vert profond, sauge et ambre.', colors: ['#0b211b', '#55c78c', '#f0b45a'], mode: 'dark' },
  { id: 'royal', name: 'Aubergine Royale', description: 'Prune, rose sophistiqué et champagne.', colors: ['#24152b', '#d66aa7', '#e7bd72'], mode: 'dark' },
  { id: 'solar', name: 'Atelier Solaire', description: 'Ivoire chaud, terre cuite et bleu encre.', colors: ['#fbf4e8', '#9f3e27', '#183a5a'], mode: 'light' }
];

const STORAGE_KEY = 'dlr-interface-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly current = signal<ThemeId>(this.read());
  readonly themes = DLR_THEMES;

  constructor() { this.apply(this.current()); }

  select(theme: ThemeId): void {
    if (!DLR_THEMES.some(option => option.id === theme)) return;
    this.current.set(theme);
    this.apply(theme);
    try { localStorage.setItem(STORAGE_KEY, theme); } catch { /* stockage privé indisponible */ }
  }

  private apply(theme: ThemeId): void {
    const option = DLR_THEMES.find(item => item.id === theme) ?? DLR_THEMES[0];
    document.documentElement.dataset['theme'] = option.id;
    document.documentElement.dataset['themeMode'] = option.mode;
    document.documentElement.style.colorScheme = option.mode;
  }

  private read(): ThemeId {
    try {
      const saved = localStorage.getItem(STORAGE_KEY) as ThemeId | null;
      if (saved && DLR_THEMES.some(option => option.id === saved)) return saved;
    } catch { /* valeur par défaut */ }
    return 'obsidian';
  }
}
