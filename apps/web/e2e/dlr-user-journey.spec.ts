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

test('un laboratoire expose concept, runner et réinitialisation', async ({ page }) => {
  await page.goto('/labs/LLM-01');
  await expect(page.getByRole('heading', { name: /prompt, tokens et sortie contrôlée/i })).toBeVisible();
  await expect(page.getByText('Concept clé · LLM-PROMPT-TOKENS')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Compiler et exécuter' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Réinitialiser ce laboratoire' })).toBeVisible();
});

test('navigation principale sans page introuvable', async ({ page }) => {
  await page.goto('/dashboard');
  await expect(page.getByRole('heading', { name: /prochaine étape/i })).toBeVisible();
  await page.getByRole('link', { name: /mon parcours/i }).click();
  await expect(page).toHaveURL(/\/paths$/);
  await expect(page.getByRole('heading', { name: 'Parcours professionnels' })).toBeVisible();
});
