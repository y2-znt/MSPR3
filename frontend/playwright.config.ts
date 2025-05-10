import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: !process.env['CI'], // Désactiver les tests parallèles dans CI
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: process.env['CI'] ? 1 : undefined,
  reporter: 'html',
  webServer: {
    command: 'npm run start -- --port=' + (process.env['CI'] ? '4201' : '4200'),
    port: process.env['CI'] ? 4201 : 4200,
    timeout: 120000,
    reuseExistingServer: !process.env['CI'],
  },
  use: {
    baseURL: 'http://localhost:' + (process.env['CI'] ? '4201' : '4200'),
    actionTimeout: 60000,
    navigationTimeout: 60000,
    trace: 'on-first-retry',
    ignoreHTTPSErrors: true,
    launchOptions: {
      args: process.env['CI'] ? [] : ['--no-sandbox'],
    },
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        launchOptions: {
          firefoxUserPrefs: {
            'network.http.connection-timeout': 30000,
            'network.http.max-connections-per-server': 10,
            'network.http.max-persistent-connections-per-server': 10,
          },
        },
      },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],
});