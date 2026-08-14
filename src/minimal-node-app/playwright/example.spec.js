const { test, expect } = require('@playwright/test');

test('opens leadtools.com', async ({ page }) => {
  await page.goto('https://leadtools.com');
  await expect(page).toHaveTitle(/FOOBARFOOBAR/);
});