import { expect, test } from '@playwright/test';

test('catalogue complet puis ouverture du parcours Learn LLMs', async ({ page, request }) => {
  const health = await request.get('http://127.0.0.1:8081/actuator/health');
  expect(health.ok()).toBeTruthy();
  expect((await health.json()).status).toBe('UP');

  await page.goto('/paths');
  await expect(page.getByRole('heading', { name: 'Parcours professionnels' })).toBeVisible();
  const llmCard = page.getByRole('button', { name: /Learn LLMs/ });
  await expect(llmCard).toBeVisible();
  await expect(llmCard.getByText('12 disponibles / 12 prévues')).toBeVisible();
  await llmCard.click();
  await expect(page.getByLabel('Progression LEARN_LLM').getByRole('heading', { name: 'Learn LLMs' })).toBeVisible();
  await expect(page.getByText('LLM-12', { exact: false })).toBeVisible();
});

test('catalogue Architecture Système avec projets et défi final', async ({ page }) => {
  await page.goto('/paths');
  const architectureCard = page.getByRole('button', { name: /Architecture Système/ });
  await expect(architectureCard.getByText('24 disponibles / 24 prévues')).toBeVisible();
  await architectureCard.click();
  await expect(page.getByLabel('Progression ARCHITECTURE').getByRole('heading', { name: 'Architecture Système' })).toBeVisible();
  await expect(page.getByLabel('Activités du parcours ARCHITECTURE').getByText('ARCHITECTURE-24 · VERROUILLÉ')).toBeVisible();
});

test('un laboratoire expose concept, runner et réinitialisation', async ({ page }) => {
  await page.goto('/labs/LLM-01');
  await expect(page.getByRole('heading', { name: /prompt, tokens et sortie contrôlée/i })).toBeVisible();
  await expect(page.getByText('Concept clé · LLM-PROMPT-TOKENS')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Compiler et exécuter' })).toBeEnabled();
  await expect(page.getByRole('button', { name: 'Réinitialiser ce laboratoire' })).toBeVisible();
});

test('navigation principale sans page introuvable', async ({ page }) => {
  await page.goto('/dashboard');
  await expect(page.getByRole('heading', { name: /prochaine étape/i })).toBeVisible();
  await page.getByRole('link', { name: /mon parcours/i }).click();
  await expect(page).toHaveURL(/\/paths$/);
  await expect(page.getByRole('heading', { name: 'Parcours professionnels' })).toBeVisible();
});

test('le planning ne date que la prochaine activité disponible', async ({ page }) => {
  await page.goto('/planning');
  await expect(page.getByRole('heading', { name: 'Planning dynamique' })).toBeVisible();
  const timeline = page.getByLabel('Planning glissant JAVA');
  await expect(timeline).toBeVisible();
  await expect(timeline.getByText('Prochaine date effective')).toHaveCount(1);
  await expect(timeline.getByText('Date calculée à la validation').first()).toBeVisible();

  await page.getByLabel('Parcours').selectOption('SQL');
  await expect(page.getByLabel('Planning glissant SQL')).toBeVisible();
});

test('les six thèmes restent lisibles et persistent', async ({ page }) => {
  await page.goto('/settings');
  const themes = ['obsidian', 'monochrome', 'boreal', 'emerald', 'royal', 'solar'];
  await expect(page.getByRole('radio')).toHaveCount(6);

  for (const theme of themes) {
    await page.locator(`[data-preview="${theme}"]`).click();
    await expect(page.locator('html')).toHaveAttribute('data-theme', theme);
    const ratios = await page.evaluate(() => {
      const style = getComputedStyle(document.documentElement);
      const parse = (value: string) => {
        const context = document.createElement('canvas').getContext('2d')!;
        context.fillStyle = value.trim();
        const normalized = context.fillStyle;
        const hex = normalized.startsWith('#') ? normalized.slice(1) : '000000';
        const full = hex.length === 3 ? [...hex].map(char => char + char).join('') : hex;
        return [0, 2, 4].map(index => parseInt(full.slice(index, index + 2), 16));
      };
      const luminance = (value: string) => {
        const channels = parse(value).map(channel => channel / 255).map(channel => channel <= .04045 ? channel / 12.92 : ((channel + .055) / 1.055) ** 2.4);
        return .2126 * channels[0] + .7152 * channels[1] + .0722 * channels[2];
      };
      const contrast = (first: string, second: string) => {
        const [high, low] = [luminance(first), luminance(second)].sort((a, b) => b - a);
        return (high + .05) / (low + .05);
      };
      const variable = (name: string) => style.getPropertyValue(name);
      return {
        body: contrast(variable('--text'), variable('--background')),
        muted: contrast(variable('--text-muted'), variable('--surface')),
        input: contrast(variable('--text'), variable('--input-background')),
        button: contrast(variable('--on-accent'), variable('--accent'))
      };
    });
    expect(ratios.body, `${theme}: texte principal`).toBeGreaterThanOrEqual(4.5);
    expect(ratios.muted, `${theme}: texte secondaire`).toBeGreaterThanOrEqual(4.5);
    expect(ratios.input, `${theme}: champs`).toBeGreaterThanOrEqual(4.5);
    expect(ratios.button, `${theme}: boutons`).toBeGreaterThanOrEqual(4.5);
  }

  await page.reload();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'solar');
});

test('le déploiement cloud conserve le brouillon sans simuler un runner', async ({ page }) => {
  await page.route('**/runtime-config.js', (route) => route.fulfill({
    contentType: 'application/javascript',
    body: `window.__DLR_CONFIG__ = {
      apiBaseUrl: 'http://localhost:8081/api',
      executionAvailable: false,
      environment: 'render-test'
    };`
  }));

  await page.goto('/labs/JAVA-01');
  await expect(page.getByText('Runner local non connecté')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Runner hors ligne' })).toBeDisabled();
  await expect(page.getByRole('button', { name: 'Validation disponible avec le Runner' })).toBeDisabled();
  await expect(page.getByText('Brouillon actif · démarre le service hybride pour exécuter')).toBeVisible();
});
