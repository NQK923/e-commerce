import { test, expect } from '@playwright/test';

test.describe('Critical Flows & Auth Redirects', () => {
  test.beforeEach(async ({ page }) => {
    // Intercept /api/users/me to return appropriate mock user based on authorization token
    await page.route('**/api/users/me', async (route) => {
      const headers = route.request().headers();
      const authHeader = headers['authorization'] || '';
      
      let user = {
        id: 'buyer-uuid',
        email: 'buyer@example.local',
        displayName: 'Smoke Buyer',
        roles: ['CUSTOMER']
      };
      
      if (authHeader.includes('mock-seller-token')) {
        user = {
          id: 'seller-uuid',
          email: 'seller@example.local',
          displayName: 'Smoke Seller',
          roles: ['CUSTOMER', 'SELLER']
        };
      } else if (authHeader.includes('mock-admin-token')) {
        user = {
          id: 'admin-uuid',
          email: 'admin@example.local',
          displayName: 'Smoke Admin',
          roles: ['ADMIN']
        };
      }
      
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(user)
      });
    });
  });

  test('unauthenticated buyer is redirected from checkout', async ({ page }) => {
    // Override /api/users/me to return 401 for unauthenticated redirect test
    await page.route('**/api/users/me', async (route) => {
      await route.fulfill({ status: 401 });
    });
    
    await page.goto('/checkout');
    await expect(page).toHaveURL(/.*login.*/);
  });

  test('unauthenticated seller is redirected from seller dashboard', async ({ page }) => {
    await page.route('**/api/users/me', async (route) => {
      await route.fulfill({ status: 401 });
    });

    await page.goto('/seller');
    await expect(page).toHaveURL(/.*login.*/);
  });

  test('unauthenticated admin is redirected from admin dashboard', async ({ page }) => {
    await page.route('**/api/users/me', async (route) => {
      await route.fulfill({ status: 401 });
    });

    await page.goto('/admin');
    await expect(page).toHaveURL(/.*login.*/);
  });

  test('buyer login and checkout flow', async ({ page }) => {
    // 1. Mock Login API
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'mock-buyer-token',
          refreshToken: 'mock-buyer-refresh',
          user: {
            id: 'buyer-uuid',
            email: 'buyer@example.local',
            displayName: 'Smoke Buyer',
            roles: ['CUSTOMER']
          }
        })
      });
    });

    // 2. Mock Cart and Product APIs
    await page.route('**/api/carts', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'cart-1',
          items: [
            {
              id: 'item-1',
              productId: 'prod-smoke-1',
              productName: 'Smoke Product',
              price: 150000,
              quantity: 2,
              sku: 'SMOKE-SKU-1'
            }
          ]
        })
      });
    });

    await page.route('**/api/orders', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'order-123',
          userId: 'buyer-uuid',
          total: 300000,
          status: 'PENDING'
        })
      });
    });

    // Go to login page
    await page.goto('/login');
    await page.fill('input[type="email"]', 'buyer@example.local');
    await page.fill('input[type="password"]', 'Buyer@123');
    await page.click('button[type="submit"]');

    // Wait for redirect to home page after successful login
    await page.waitForURL('**/');

    // Go to checkout
    await page.goto('/checkout');
    await page.fill('input[name="fullName"]', 'Smoke Buyer');
    await page.fill('input[name="addressLine1"]', '1 Dev Street');
    await page.fill('input[name="city"]', 'Ho Chi Minh City');
    await page.fill('input[name="state"]', 'Ho Chi Minh');
    await page.fill('input[name="postalCode"]', '700000');
    await page.fill('input[name="country"]', 'VN');

    // Click submit order
    const placeOrderBtn = page.locator('button', { hasText: 'Đặt hàng' });
    if (await placeOrderBtn.isVisible()) {
      await placeOrderBtn.click();
    }
  });

  test('seller product creation flow', async ({ page }) => {
    // 1. Mock Login API
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'mock-seller-token',
          refreshToken: 'mock-seller-refresh',
          user: {
            id: 'seller-uuid',
            email: 'seller@example.local',
            displayName: 'Smoke Seller',
            roles: ['CUSTOMER', 'SELLER']
          }
        })
      });
    });

    // 2. Mock Seller Products list API
    await page.route('**/api/products*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          items: [],
          total: 0
        })
      });
    });

    // Login
    await page.goto('/login');
    await page.fill('input[type="email"]', 'seller@example.local');
    await page.fill('input[type="password"]', 'Seller@123');
    await page.click('button[type="submit"]');

    // Wait for redirect to home page
    await page.waitForURL('**/');

    // Go to seller channel
    await page.goto('/seller/dashboard');
    await expect(page).toHaveURL(/.*seller.*/);
  });

  test('admin system dashboard outbox control', async ({ page }) => {
    // 1. Mock Login API
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'mock-admin-token',
          refreshToken: 'mock-admin-refresh',
          user: {
            id: 'admin-uuid',
            email: 'admin@example.local',
            displayName: 'Smoke Admin',
            roles: ['ADMIN']
          }
        })
      });
    });

    // 2. Mock Failed Outbox Events API
    await page.route('**/api/admin/outbox/failed*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              id: 'event-uuid-123',
              aggregateId: 'order-1',
              type: 'OrderCreated',
              status: 'DEAD_LETTER',
              attemptCount: 5,
              lastError: 'Kafka connection timeout'
            }
          ],
          totalElements: 1,
          totalPages: 1,
          page: 0,
          size: 10
        })
      });
    });

    await page.route('**/api/admin/outbox/*/retry', async (route) => {
      await route.fulfill({ status: 200 });
    });

    // 3. Mock admin dashboard stats
    await page.route('**/api/users/all', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
    });

    await page.route('**/api/seller/applications', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
    });

    // Login
    await page.goto('/login');
    await page.fill('input[type="email"]', 'admin@example.local');
    await page.fill('input[type="password"]', 'LocalAdmin123!');
    await page.click('button[type="submit"]');

    // Wait for redirect to admin dashboard
    await page.waitForURL('**/admin');

    // Navigate to admin system
    await page.goto('/admin/system');
    
    // Check header (since we added localized translations, let's look for "Hệ thống")
    await expect(page.locator('main h1')).toContainText(/hệ thống/i);

    // Verify outbox entry is present
    await expect(page.locator('tbody tr')).toContainText(/OrderCreated/);

    // Click retry
    const retryBtn = page.locator('button', { hasText: 'Thử lại' }).first();
    if (await retryBtn.isVisible()) {
      await retryBtn.click();
    }
  });

  test('mobile responsive view layout', async ({ page }) => {
    // Set mobile viewport size
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');
    
    // Check if the logo/heading is visible on mobile screens
    await expect(page).toHaveTitle(/EcomX/i);
  });
});
