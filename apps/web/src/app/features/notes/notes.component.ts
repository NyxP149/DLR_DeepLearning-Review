import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, map, of, startWith } from 'rxjs';

import { NotesApiService, PersonalNote } from '../../core/api/notes-api.service';

type NotesState =
  | { status: 'loading'; notes: PersonalNote[] }
  | { status: 'loaded'; notes: PersonalNote[] }
  | { status: 'error'; notes: PersonalNote[] };

@Component({
  selector: 'dlr-notes',
  imports: [AsyncPipe, DatePipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Carnet d’apprentissage</p>
      <h1>Mes notes personnelles</h1>
      <span>Retrouve tes réflexions par langage et par laboratoire.</span>
    </header>

    @if (state$ | async; as state) {
      @if (state.status === 'loading') {
        <p class="state">Chargement des notes…</p>
      } @else if (state.status === 'error') {
        <p class="state error" role="alert">Les notes ne peuvent pas être chargées. Vérifie la connexion à l’API.</p>
      } @else if (state.notes.length === 0) {
        <section class="empty">
          <strong>Ton carnet est encore vide</strong>
          <p>Écris une note personnelle depuis un laboratoire : elle apparaîtra ici automatiquement.</p>
          <a routerLink="/paths">Choisir un laboratoire →</a>
        </section>
      } @else {
        @for (language of languages(state.notes); track language) {
          <section class="language-group" [attr.aria-label]="'Notes ' + language">
            <div class="group-title">
              <div><p>Langage</p><h2>{{ languageLabel(language) }}</h2></div>
              <span>{{ notesFor(state.notes, language).length }} note{{ notesFor(state.notes, language).length > 1 ? 's' : '' }}</span>
            </div>
            <div class="note-grid">
              @for (note of notesFor(state.notes, language); track note.labCode) {
                <article class="note-card">
                  <div class="meta">
                    <span>{{ note.labCode }}</span>
                    <span class="status" [class.completed]="note.completed">{{ note.completed ? 'Laboratoire terminé' : 'En cours' }}</span>
                  </div>
                  <h3>{{ note.labTitle }}</h3>
                  <p class="content">{{ note.content }}</p>
                  <footer>
                    <small>{{ note.updatedAt ? ('Modifiée le ' + (note.updatedAt | date:'dd/MM/yyyy à HH:mm')) : 'Note locale' }}</small>
                    <a [routerLink]="['/labs', note.labCode]">Ouvrir le labo →</a>
                  </footer>
                </article>
              }
            </div>
          </section>
        }
      }
    }
  `,
  styles: [`
    :host{display:block}.page-header{margin-bottom:1.5rem}.page-header p,.group-title p{color:var(--accent);font-size:.75rem;font-weight:800;letter-spacing:.09em;margin:0;text-transform:uppercase}.page-header h1{font-size:clamp(2rem,4vw,3rem);margin:.25rem 0}.page-header span,.note-card h3,.state,.empty p{color:var(--text-muted)}.state,.empty{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:1.2rem}.state.error{color:var(--danger)}.empty a,.note-card a{color:var(--link);font-weight:750;text-decoration:none}.language-group{margin-top:1.4rem}.group-title{align-items:end;display:flex;justify-content:space-between;margin-bottom:.7rem}.group-title h2{font-size:1.45rem;margin:.2rem 0 0}.group-title>span{background:var(--accent-soft);border-radius:99px;color:var(--link);font-size:.72rem;font-weight:750;padding:.35rem .65rem}.note-grid{display:grid;gap:.8rem;grid-template-columns:repeat(auto-fit,minmax(min(100%,340px),1fr))}.note-card{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);display:flex;flex-direction:column;padding:1rem}.meta,footer{align-items:center;display:flex;gap:.7rem;justify-content:space-between}.meta>span:first-child{color:var(--accent);font-size:.72rem;font-weight:800}.status{background:var(--surface-raised);border-radius:99px;color:var(--text-muted);font-size:.65rem;padding:.3rem .5rem}.status.completed{background:var(--success-soft);color:var(--success)}.note-card h3{font-size:.85rem;margin:.8rem 0 .35rem}.content{line-height:1.65;margin:.4rem 0 1rem;white-space:pre-wrap}.note-card footer{border-top:1px solid var(--border);margin-top:auto;padding-top:.8rem}.note-card small{color:var(--text-muted);font-size:.67rem}@media(max-width:600px){.group-title,.note-card footer{align-items:flex-start;flex-direction:column}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotesComponent {
  private readonly api = inject(NotesApiService);

  readonly state$ = this.api.list().pipe(
    map((notes) => ({ status: 'loaded' as const, notes })),
    startWith({ status: 'loading' as const, notes: [] as PersonalNote[] }),
    catchError(() => of({ status: 'error' as const, notes: [] as PersonalNote[] }))
  );

  languages(notes: PersonalNote[]): string[] {
    return [...new Set(notes.map((note) => note.language))].sort();
  }

  notesFor(notes: PersonalNote[], language: string): PersonalNote[] {
    return notes.filter((note) => note.language === language)
      .sort((first, second) => Number(second.completed) - Number(first.completed));
  }

  languageLabel(language: string): string {
    return ({ SPRING_BOOT: 'Spring Boot', LEARN_LLM: 'Learn LLMs', DEVOPS: 'Docker & CI/CD' } as Record<string, string>)[language]
      ?? language.replaceAll('_', ' ');
  }
}
