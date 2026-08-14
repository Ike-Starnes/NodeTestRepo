const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './playwright',

  outputDir: 'playwright-artifacts',

  reporter: [
    ['list'],
    ['junit', {
      outputFile: 'test-results/playwright/playwright-junit.xml'
    }],
    ['html', {
      outputFolder: 'playwright-report',
      open: 'never'
    }]
  ]
});
