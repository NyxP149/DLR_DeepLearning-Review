import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, ViewChild, signal } from '@angular/core';
import type { Compartment, Extension } from '@codemirror/state';
import type { EditorView } from '@codemirror/view';

interface CodeMirrorModules {
  view: typeof import('@codemirror/view');
  state: typeof import('@codemirror/state');
  commands: typeof import('@codemirror/commands');
  language: typeof import('@codemirror/language');
  search: typeof import('@codemirror/search');
  autocomplete: typeof import('@codemirror/autocomplete');
  highlight: typeof import('@lezer/highlight');
  java: typeof import('@codemirror/lang-java');
  python: typeof import('@codemirror/lang-python');
  javascript: typeof import('@codemirror/lang-javascript');
}

@Component({
  selector: 'dlr-code-editor',
  template: `
    @if (fallback()) {
      <textarea
        class="fallback-editor"
        spellcheck="false"
        [value]="value"
        (input)="emitFallback($event)"
        (keydown)="onFallbackKeydown($event)"
        [attr.aria-label]="'Code ' + language"
      ></textarea>
    } @else {
      <div #host class="editor-host"></div>
      @if (loading()) { <p class="loading" aria-live="polite">Chargement de l'éditeur…</p> }
    }
  `,
  styles: [`
    :host { display: block; margin: .5rem 0 1rem; }
    .editor-host, .fallback-editor {
      background: var(--editor-background);
      border: 1px solid var(--border);
      border-radius: .75rem;
      min-height: 320px;
      overflow: hidden;
      width: 100%;
    }
    .fallback-editor {
      color: var(--editor-text);
      font: .86rem/1.6 ui-monospace, SFMono-Regular, Consolas, monospace;
      padding: 1rem;
      resize: vertical;
      tab-size: 4;
    }
    .fallback-editor:focus { border-color: var(--accent); outline: none; }
    .loading { color: var(--text-muted); font-size: .78rem; margin: .45rem 0 0; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CodeEditorComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() value = '';
  @Input() language = 'JAVA';
  @Input() simple = false;
  @Output() readonly valueChange = new EventEmitter<string>();
  @ViewChild('host') private host?: ElementRef<HTMLDivElement>;

  readonly loading = signal(true);
  readonly fallback = signal(false);
  private view?: EditorView;
  private cm?: CodeMirrorModules;
  private dynamicSettings?: Compartment;
  private applyingExternalValue = false;
  private releaseTab = false;

  async ngAfterViewInit(): Promise<void> {
    if (this.simple) {
      this.fallback.set(true);
      this.loading.set(false);
      return;
    }

    try {
      const [view, state, commands, language, search, autocomplete, highlight, java, python, javascript] = await Promise.all([
        import('@codemirror/view'),
        import('@codemirror/state'),
        import('@codemirror/commands'),
        import('@codemirror/language'),
        import('@codemirror/search'),
        import('@codemirror/autocomplete'),
        import('@lezer/highlight'),
        import('@codemirror/lang-java'),
        import('@codemirror/lang-python'),
        import('@codemirror/lang-javascript')
      ]);
      if (!this.host) return;
      this.cm = { view, state, commands, language, search, autocomplete, highlight, java, python, javascript };
      this.dynamicSettings = new state.Compartment();
      this.view = new view.EditorView({
        parent: this.host.nativeElement,
        state: state.EditorState.create({ doc: this.value, extensions: this.extensions() })
      });
      this.loading.set(false);
    } catch {
      this.fallback.set(true);
      this.loading.set(false);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.view || !this.cm || !this.dynamicSettings) return;
    if (changes['language']) {
      this.view.dispatch({ effects: this.dynamicSettings.reconfigure(this.languageSettings()) });
    }
    if (changes['value'] && this.view.state.doc.toString() !== this.value) {
      this.applyingExternalValue = true;
      this.view.dispatch({ changes: { from: 0, to: this.view.state.doc.length, insert: this.value } });
      this.applyingExternalValue = false;
    }
  }

  emitFallback(event: Event): void {
    this.valueChange.emit((event.target as HTMLTextAreaElement).value);
  }

  onFallbackKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.releaseTab = true;
      return;
    }
    if (event.key !== 'Tab' || event.shiftKey || event.ctrlKey || event.altKey || event.metaKey || this.releaseTab) {
      this.releaseTab = false;
      return;
    }
    event.preventDefault();
    const area = event.target as HTMLTextAreaElement;
    area.setRangeText('    ', area.selectionStart, area.selectionEnd, 'end');
    this.valueChange.emit(area.value);
  }

  private extensions(): Extension[] {
    const { view, state, commands, language, search, autocomplete } = this.cm!;
    return [
      view.lineNumbers(),
      view.highlightActiveLineGutter(),
      view.highlightActiveLine(),
      view.drawSelection(),
      view.dropCursor(),
      commands.history(),
      language.indentOnInput(),
      language.bracketMatching(),
      autocomplete.closeBrackets(),
      language.indentUnit.of('    '),
      state.EditorState.tabSize.of(4),
      view.EditorView.lineWrapping,
      language.syntaxHighlighting(this.highlightStyle()),
      this.theme(),
      view.keymap.of([
        ...autocomplete.closeBracketsKeymap,
        ...commands.defaultKeymap,
        ...search.searchKeymap,
        ...commands.historyKeymap,
        commands.indentWithTab
      ]),
      this.dynamicSettings!.of(this.languageSettings()),
      view.EditorView.updateListener.of((update) => {
        if (update.docChanged && !this.applyingExternalValue) this.valueChange.emit(update.state.doc.toString());
      })
    ];
  }

  private languageSettings(): Extension {
    const { view, java, python, javascript } = this.cm!;
    const syntax = ({
      JAVA: () => java.java(),
      PYTHON: () => python.python(),
      TYPESCRIPT: () => javascript.javascript({ typescript: true })
    } as Record<string, () => Extension>)[this.language.toUpperCase()];
    return [
      syntax ? syntax() : [],
      view.EditorView.contentAttributes.of({
        'aria-label': `Code ${this.language}`,
        autocapitalize: 'off',
        autocorrect: 'off',
        spellcheck: 'false'
      })
    ];
  }

  private highlightStyle() {
    const { language, highlight } = this.cm!;
    const t = highlight.tags;
    return language.HighlightStyle.define([
      { tag: [t.keyword, t.controlKeyword, t.modifier, t.operatorKeyword, t.definitionKeyword, t.moduleKeyword], color: '#c792ea' },
      { tag: [t.typeName, t.className, t.namespace], color: '#ffcb6b' },
      { tag: [t.string, t.character, t.regexp], color: '#c3e88d' },
      { tag: [t.number, t.bool, t.null, t.atom], color: '#f78c6c' },
      { tag: [t.lineComment, t.blockComment, t.docComment], color: '#8a9bb3', fontStyle: 'italic' },
      { tag: [t.function(t.variableName), t.function(t.propertyName), t.definition(t.function(t.variableName))], color: '#82aaff' },
      { tag: [t.operator, t.derefOperator], color: '#89ddff' },
      { tag: [t.propertyName], color: '#f07178' },
      { tag: [t.meta, t.annotation], color: '#ffd866' },
      { tag: t.invalid, color: '#ff6b6b' }
    ]);
  }

  private theme(): Extension {
    const tint = (percent: number) => `color-mix(in srgb, var(--accent) ${percent}%, transparent)`;
    return this.cm!.view.EditorView.theme({
      '&': { backgroundColor: 'var(--editor-background)', color: 'var(--editor-text)', fontSize: '14px', maxHeight: '70vh', minHeight: '320px' },
      '&.cm-focused': { outline: '2px solid color-mix(in srgb, var(--focus-ring) 68%, transparent)', outlineOffset: '-2px' },
      '.cm-scroller': { fontFamily: "'JetBrains Mono', 'Cascadia Code', Consolas, monospace", lineHeight: '1.6', minHeight: '320px', overflow: 'auto' },
      '.cm-content': { caretColor: 'var(--accent)', padding: '14px 0' },
      '.cm-content:focus-visible': { outline: 'none' },
      '.cm-line': { padding: '0 14px' },
      '.cm-cursor, .cm-dropCursor': { borderLeftColor: 'var(--accent)', borderLeftWidth: '2px' },
      '.cm-selectionBackground': { backgroundColor: `${tint(35)} !important` },
      '&.cm-focused .cm-selectionBackground': { backgroundColor: `${tint(45)} !important` },
      '.cm-activeLine': { backgroundColor: tint(8) },
      '.cm-gutters': { backgroundColor: 'transparent', border: '0', color: 'var(--text-muted)' },
      '.cm-activeLineGutter': { backgroundColor: tint(8), color: 'var(--editor-text)' },
      '.cm-matchingBracket, .cm-nonmatchingBracket': { backgroundColor: tint(25), outline: `1px solid ${tint(60)}` },
      '.cm-panels': { backgroundColor: 'var(--surface-raised)', color: 'var(--text)' },
      '.cm-searchMatch': { backgroundColor: tint(30) }
    }, { dark: true });
  }

  ngOnDestroy(): void {
    this.view?.destroy();
    this.view = undefined;
    this.cm = undefined;
  }
}
