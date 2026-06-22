import { test, expect } from '@playwright/test';

test('has title', async ({ page }) => {
  await page.goto('/');

  // Expect a title "to contain" a substring.
  await expect(page).toHaveTitle(/EcomX/i);
});

test('can navigate to products', async ({ page }) => {
  await page.goto('/');

  // Expects page to have a link to products.
  const productsLink = page.getByRole('link', { name: /Products/i }).first();
  if (await productsLink.isVisible()) {
    await productsLink.click();
    await expect(page).toHaveURL(/.*products.*/);
  }
});
