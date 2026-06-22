import { test, expect } from '@playwright/test';

test.describe('Auth Regression Tests', () => {
  test('should load login page', async ({ page }) => {
    await page.goto('/login');
    await expect(page).toHaveTitle(/EcomX/i);
    await expect(page.getByRole('heading').first()).toBeVisible();
  });

  test('should load register page', async ({ page }) => {
    await page.goto('/register');
    await expect(page).toHaveTitle(/EcomX/i);
    await expect(page.getByRole('heading').first()).toBeVisible();
  });
});
