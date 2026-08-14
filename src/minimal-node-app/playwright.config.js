const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests',

  outputDir: 'playwright-artifacts',

  reporter: [
    ['list'],
    ['junit', {
      outputFile: 'test-results/playwright/playwright-junit.xml'
    }],
    ['html', {
      outputFolder: 'test-results/playwright/report',
      open: 'never'
    }]
  ]
});
