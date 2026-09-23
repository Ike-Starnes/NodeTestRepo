const { test, expect } = require('@playwright/test');
const { allure } = require('allure-playwright');
const fs = require('fs');

test('Download PNG directly', async ({ request }, testInfo) => {
  const response = await request.get(
    'https://www.leadtools.com/help/sdk/resources/images/netleadtoolstopics/singlelayout1.png'
  );

  expect(response.ok()).toBeTruthy();

  const buffer = await response.body();
  fs.writeFileSync('downloaded-image.png', buffer);

  // Attach to Allure report
  await testInfo.attach('Downloaded PNG', {
    body: buffer,
    contentType: 'image/png',
  });

  console.log('PNG saved as downloaded-image.png');
});

test('opens leadtools.com', async ({ page }) => {
  await page.goto('https://leadtools.com');
  await expect(page).toHaveTitle(/LEADTOOLS/);
});