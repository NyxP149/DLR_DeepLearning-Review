import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, ViewChild, signal } from '@angular/core';
import type { editor as MonacoEditor } from 'monaco-editor/editor';

@Component({
  selector: 'dlr-code-editor',
  template: `
    @if (fallback()) {
      <textarea
        class="fallback-editor"
        spellcheck="false"
        [value]="value"
        (input)="emitFallback($event)"
        [attr.aria-label]="'Code ' + language"
      ></textarea>
    } @else {
      <div #host class="monaco-host" [attr.aria-label]="'Éditeur de code ' + language"></div>
      @if (loading()) { <p class="loading" aria-live="polite">Chargement de Monaco…</p> }
    }
  `,
  styles: [`
    :host { display: block; margin: .5rem 0 1rem; }
    .monaco-host, .fallback-editor {
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
  @Output() readonly valueChange = new EventEmitter<string>();
  @ViewChild('host') private host?: ElementRef<HTMLDivElement>;

  readonly loading = signal(true);
  readonly fallback = signal(false);
  private editor?: MonacoEditor.IStandaloneCodeEditor;
  private monaco?: typeof import('monaco-editor/editor');
  private resizeObserver?: ResizeObserver;
  private themeObserver?: MutationObserver;
  private applyingExternalValue = false;

  async ngAfterViewInit(): Promise<void> {
    if (window.matchMedia('(max-width: 700px)').matches || typeof Worker === 'undefined') {
      this.fallback.set(true);
      this.loading.set(false);
      return;
    }

    try {
      const workerUrl = new URL('monaco-editor/editor.worker', import.meta.url);
      (globalThis as typeof globalThis & { MonacoEnvironment?: unknown }).MonacoEnvironment = {
        getWorker: () => new Worker(workerUrl, { type: 'module', name: 'dlr-monaco-editor' })
      };
      const [monaco] = await Promise.all([
        import('monaco-editor/editor'),
        import('monaco-editor/features/codeEditor/register'),
        import('monaco-editor/features/bracketMatching/register'),
        import('monaco-editor/features/clipboard/register'),
        import('monaco-editor/features/comment/register'),
        import('monaco-editor/features/find/register'),
        import('monaco-editor/features/folding/register'),
        import('monaco-editor/features/indentation/register'),
        import('monaco-editor/features/lineSelection/register'),
        import('monaco-editor/features/linesOperations/register'),
        import('monaco-editor/features/multicursor/register'),
        import('monaco-editor/features/wordOperations/register'),
        import('monaco-editor/features/wordPartOperations/register'),
        import('monaco-editor/languages/definitions/java/register'),
        import('monaco-editor/languages/definitions/python/register'),
        import('monaco-editor/languages/definitions/typescript/register')
      ]);
      if (!this.host) return;
      this.monaco = monaco;
      this.applyMonacoTheme();
      this.editor = monaco.editor.create(this.host.nativeElement, {
        value: this.value,
        language: this.monacoLanguage(),
        theme: 'dlr-adaptive',
        automaticLayout: false,
        fontFamily: 'JetBrains Mono, Cascadia Code, Consolas, monospace',
        fontSize: 14,
        minimap: { enabled: false },
        padding: { top: 14, bottom: 14 },
        scrollBeyondLastLine: false,
        tabSize: 4,
        wordWrap: 'on'
      });
      this.editor.onDidChangeModelContent(() => {
        if (!this.applyingExternalValue) this.valueChange.emit(this.editor?.getValue() ?? '');
      });
      this.resizeObserver = new ResizeObserver(() => this.editor?.layout());
      this.resizeObserver.observe(this.host.nativeElement);
      this.themeObserver = new MutationObserver(() => this.applyMonacoTheme());
      this.themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme', 'data-theme-mode'] });
      this.loading.set(false);
    } catch {
      this.fallback.set(true);
      this.loading.set(false);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['language'] && this.editor && this.monaco) {
      const model = this.editor.getModel();
      if (model) this.monaco.editor.setModelLanguage(model, this.monacoLanguage());
    }
    if (changes['value'] && this.editor && this.editor.getValue() !== this.value) {
      this.applyingExternalValue = true;
      this.editor.setValue(this.value);
      this.applyingExternalValue = false;
    }
  }

  emitFallback(event: Event): void {
    this.valueChange.emit((event.target as HTMLTextAreaElement).value);
  }

  private monacoLanguage(): string {
    return ({ JAVA: 'java', PYTHON: 'python', TYPESCRIPT: 'typescript' } as Record<string, string>)[this.language.toUpperCase()] ?? 'plaintext';
  }

  private applyMonacoTheme(): void {
    if (!this.monaco) return;
    const styles = getComputedStyle(document.documentElement);
    const color = (name: string) => styles.getPropertyValue(name).trim();
    this.monaco.editor.defineTheme('dlr-adaptive', {
      base: document.documentElement.dataset['themeMode'] === 'light' ? 'vs' : 'vs-dark',
      inherit: true,
      rules: [],
      colors: {
        'editor.background': color('--editor-background'),
        'editor.foreground': color('--editor-text'),
        'editorLineNumber.foreground': color('--text-muted'),
        'editorCursor.foreground': color('--accent'),
        'editor.selectionBackground': color('--accent-soft'),
        'editor.inactiveSelectionBackground': color('--surface-raised')
      }
    });
    this.monaco.editor.setTheme('dlr-adaptive');
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.themeObserver?.disconnect();
    this.editor?.dispose();
    this.monaco = undefined;
  }
}
