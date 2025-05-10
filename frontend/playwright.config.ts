// import { defineConfig, devices } from '@playwright/test';

// /**
//  * Read environment variables from file.
//  * https://github.com/motdotla/dotenv
//  */
// // import dotenv from 'dotenv';
// // import path from 'path';
// // dotenv.config({ path: path.resolve(__dirname, '.env') });

// /**
//  * See https://playwright.dev/docs/test-configuration.
//  */
// export default defineConfig({
//   testDir: './tests',
//   /* Run tests in files in parallel */
//   fullyParallel: true,
//   /* Fail the build on CI if you accidentally left test.only in the source code. */
//   forbidOnly: !!process.env['CI'],
//   /* Retry on CI only */
//   retries: process.env['CI'] ? 2 : 0,
//   /* Opt out of parallel tests on CI. */
//   workers: process.env['CI'] ? 1 : undefined,
//   /* Reporter to use. See https://playwright.dev/docs/test-reporters */
//   reporter: 'html',
//   /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
//   webServer: {
//     command: 'npm run start', // ou 'ng serve' si tu n'as pas de script custom
//     port: 4200,
//     timeout: 60000,
//     reuseExistingServer: !process.env['CI'], // réutiliser le serveur local hors CI
//   },
//   use: {
//     /* Base URL to use in actions like `await page.goto('/')`. */
//     baseURL: 'http://localhost:4200',
//     actionTimeout: 60000,
//     navigationTimeout: 60000,
//     trace: 'on-first-retry',
//     ignoreHTTPSErrors: true,
//     launchOptions: {
//       args: ['--no-sandbox', '--disable-setuid-sandbox']
//     }
//   },

//   /* Configure projects for major browsers */
//   projects: [
//     {
//       name: 'chromium',
//       use: { ...devices['Desktop Chrome'] },
//     },

//     {
//       name: 'firefox',
//       use: { 
//         ...devices['Desktop Firefox'],
//         launchOptions: {
//           firefoxUserPrefs: {
//             'network.http.connection-timeout': 30000,
//             'network.http.max-connections-per-server': 10,
//             'network.http.max-persistent-connections-per-server': 10
//           }
//         }
//       },
//     },

//     {
//       name: 'webkit',
//       use: { ...devices['Desktop Safari'] },
//     },

//     /* Test against mobile viewports. */
//     // {
//     //   name: 'Mobile Chrome',
//     //   use: { ...devices['Pixel 5'] }, 
//     // },
//     // {
//     //   name: 'Mobile Safari',
//     //   use: { ...devices['iPhone 12'] },
//     // },

//     /* Test against branded browsers. */
//     // {
//     //   name: 'Microsoft Edge',
//     //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
//     // },
//     // {
//     //   name: 'Google Chrome',
//     //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
//     // },
//   ],

//   /* Run your local dev server before starting the tests */
//   // webServer: {
//   //   command: 'npm run start',
//   //   url: 'http://127.0.0.1:3000',
//   //   reuseExistingServer: !process.env.CI,
//   // },
// });
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: !process.env['CI'], // Désactiver les tests parallèles dans CI
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: process.env['CI'] ? 1 : undefined,
  reporter: 'html',
  webServer: {
    command: 'npm run start -- --port=' + (process.env['PORT'] || 4200),
    port: Number(process.env['PORT']) || 4200,
    timeout: 120000, // Augmenté pour CI
    reuseExistingServer: !process.env['CI'],
  },
  use: {
    baseURL: 'http://localhost:' + (process.env['PORT'] || 4200),
    actionTimeout: 60000,
    navigationTimeout: 60000,
    trace: 'on-first-retry',
    ignoreHTTPSErrors: true,
    launchOptions: {
      args: ['--no-sandbox', '--disable-setuid-sandbox'],
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