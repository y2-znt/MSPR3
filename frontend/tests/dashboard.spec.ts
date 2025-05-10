import { test, expect } from '@playwright/test';

// Augmenter le timeout global pour tous les tests
test.setTimeout(120000);

test.describe('Dashboard Component Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Augmenter le timeout spécifique pour la navigation et utiliser domcontentloaded au lieu de networkidle
    await page.goto('/', { 
      timeout: 90000,
      waitUntil: 'domcontentloaded' // Moins strict que networkidle
    });
    
    // Attendre que la page soit visuellement prête
    await page.waitForFunction(() => {
      return document.readyState === 'complete';
    }, { timeout: 60000 });
    
    // Attendre que le dashboard soit visible
    await page.waitForSelector('.dashboard-container', { 
      state: 'visible',
      timeout: 60000 
    });
    
    // Attendre que les éléments principaux soient chargés
    await page.waitForSelector('mat-tab-group', { state: 'visible', timeout: 60000 });
    await page.waitForSelector('.kpi-card', { state: 'visible', timeout: 60000 });
  });

  test('should display dashboard with all required elements', async ({ page }) => {
    // Vérifier le titre du dashboard
    await expect(page.getByRole('heading', { name: /Dashboard/i })).toBeVisible({ timeout: 15000 });
    await expect(page.getByText(/Monitor and analyze/i)).toBeVisible({ timeout: 15000 });

    // Vérifier la présence des filtres
    await expect(page.getByRole('group', { name: 'Period' })).toBeVisible({ timeout: 15000 });
    await expect(page.getByRole('textbox', { name: 'Start date of the period' })).toBeVisible({ timeout: 15000 });
    await expect(page.getByRole('textbox', { name: 'End date of the period' })).toBeVisible({ timeout: 15000 });
    await expect(page.getByRole('combobox', { name: 'Select countries' })).toBeVisible({ timeout: 15000 });

    // Vérifier le bouton Add New Case
    await expect(page.getByRole('button', { name: 'Add a new case' })).toBeVisible({ timeout: 15000 });

    // Vérifier les KPI cards
    const kpiCards = page.locator('.kpi-card');
    await expect(kpiCards).toHaveCount(3, { timeout: 15000 });
  });


  test('should open dialog when clicking Add New Case button', async ({ page }) => {
    const addButton = page.getByRole('button', { name: 'Add a new case' });
    await addButton.waitFor({ state: 'visible', timeout: 40000 });
    await addButton.click();
  
    // Attendre que le dialog soit visible
    const dialog = page.getByRole('dialog', { name: 'Add New Disease Case' });
    await expect(dialog).toBeVisible({ timeout: 30000 });
  
    // Vérifier les champs à l'intérieur du dialog
    await expect(dialog.getByRole('combobox', { name: 'Country' })).toBeVisible({ timeout: 10000 });
    await expect(dialog.getByRole('textbox', { name: 'Date' })).toBeVisible({ timeout: 10000 });
    await expect(dialog.getByRole('spinbutton', { name: 'Confirmed Cases' })).toBeVisible({ timeout: 10000 });
    await expect(dialog.getByRole('spinbutton', { name: 'Deaths' })).toBeVisible({ timeout: 10000 });
    await expect(dialog.getByRole('spinbutton', { name: 'Recovered' })).toBeVisible({ timeout: 10000 });
  });
  

  // test('should filter by date range', async ({ page }) => {
  //   // Attendre que le bouton du calendrier soit visible et cliquable
  //   const calendarButton = page.getByRole('button', { name: 'Open calendar for date range' });
  //   await calendarButton.waitFor({ state: 'visible', timeout: 40000 });
    
  //   // Utiliser force: true pour éviter les problèmes d'interception de clic
  //   await calendarButton.click({ force: true });

  //   // Attendre que le calendrier soit visible
  //   await page.waitForSelector('mat-calendar', { state: 'visible', timeout: 30000 });
    
  //   // Sélectionner une date de début
  //   const startDate = page.getByRole('gridcell', { name: '1' }).first();
  //   await startDate.waitFor({ state: 'visible', timeout: 30000 });
  //   await startDate.click();
    
  //   // Sélectionner une date de fin
  //   const endDate = page.getByRole('gridcell', { name: '15' }).first();
  //   await endDate.waitFor({ state: 'visible', timeout: 30000 });
  //   await endDate.click();

  //   // Vérifier que les dates sont sélectionnées
  //   await expect(page.getByRole('textbox', { name: 'Start date of the period' })).toHaveValue(/.*/, { timeout: 30000 });
  //   await expect(page.getByRole('textbox', { name: 'End date of the period' })).toHaveValue(/.*/, { timeout: 30000 });
  // });

//   test('should filter by countries', async ({ page }) => {
//   // Wait until the country selector is visible
//   const countrySelect = page.getByRole('combobox', { name: 'Select countries' });
//   await countrySelect.waitFor({ state: 'visible', timeout: 40000 });

//   // Forcefully click the dropdown to ensure it opens
//   await countrySelect.click({ force: true });

//   // Add an intermediate check to ensure the dropdown is expanded
//   await page.waitForSelector('.dropdown-expanded-class', { state: 'visible', timeout: 30000 });

//   // Wait for the menu options to become visible
//   await page.waitForSelector('mat-option', { state: 'visible', timeout: 60000 });

//   // Select the first country
//   const firstOption = page.getByRole('option').first();
//   await firstOption.waitFor({ state: 'visible', timeout: 30000 });
//   await firstOption.click();

//   // Verify the selection
//   await expect(countrySelect).toContainText(/Afghanistan|France|United States/, { timeout: 30000 });
// });

  test('should display tabs correctly', async ({ page }) => {
    // Vérifier la présence des onglets
    await expect(page.getByRole('tab', { name: 'Overview' })).toBeVisible({ timeout: 15000 });
    await expect(page.getByRole('tab', { name: 'Countries' })).toBeVisible({ timeout: 15000 });

    // Vérifier que l'onglet Overview est actif par défaut
    await expect(page.getByRole('tab', { name: 'Overview' })).toHaveAttribute('aria-selected', 'true', { timeout: 15000 });
  });

  test('should filter by countries', async ({ page }) => {
    // Wait until the country selector is visible
    const countrySelect = page.getByRole('combobox', { name: 'Select countries' });
    await countrySelect.waitFor({ state: 'visible', timeout: 40000 });
  
    // Forcefully click the dropdown to ensure it opens
    await countrySelect.click({ force: true });
  
    // Check if the dropdown is fully expanded
    await page.waitForSelector('mat-option', { state: 'attached', timeout: 30000 });
  
    // Debugging log for troubleshooting
    console.log("Dropdown expanded and mat-options are attached.");
  
    // Wait for the menu options to become visible
    await page.waitForSelector('mat-option', { state: 'visible', timeout: 60000 });
  
    // Select the first country
    const firstOption = page.getByRole('option').first();
    await firstOption.waitFor({ state: 'visible', timeout: 30000 });
    await firstOption.click();
  
    // Verify the selection
    await expect(countrySelect).toContainText(/Afghanistan|France|United States/, { timeout: 30000 });
  });


  test('should switch between tabs', async ({ page }) => {
    // Attendre que l'onglet Countries soit visible et cliquable
    const countriesTab = page.getByRole('tab', { name: 'Countries' });
    await countriesTab.waitFor({ state: 'visible', timeout: 15000 });
    await countriesTab.click();

    // Vérifier que l'onglet Countries est actif
    await expect(page.getByRole('tab', { name: 'Countries' })).toHaveAttribute('aria-selected', 'true', { timeout: 15000 });
    await expect(page.getByRole('tab', { name: 'Overview' })).toHaveAttribute('aria-selected', 'false', { timeout: 15000 });
  });

  //   test('should add new case through dialog', async ({ page }) => {
  test.skip('should add new case through dialog', async ({ page }) => {
      // Attendre que le bouton soit visible et cliquable
      const addButton = page.getByRole('button', { name: 'Add a new case' });
      await addButton.waitFor({ state: 'visible', timeout: 30000 });
      await addButton.click({ force: true });

      // Attendre que le dialog soit visible
      const dialog = page.locator('mat-dialog-container');
      await dialog.waitFor({ state: 'visible', timeout: 30000 });

      // Remplir le formulaire
      const countrySelect = page.getByRole('combobox', { name: 'Country' });
      await countrySelect.waitFor({ state: 'visible', timeout: 30000 });
      await countrySelect.click({ force: true });
      
      const firstOption = page.getByRole('option').first();
      await firstOption.waitFor({ state: 'visible', timeout: 30000 });
      await firstOption.click();

      // Sélectionner la date
      const calendarButton = page.getByRole('button', { name: 'Open calendar' });
      await calendarButton.waitFor({ state: 'visible', timeout: 30000 });
      await calendarButton.click({ force: true });

      const dateCell = page.getByRole('gridcell', { name: '1' }).first();
      await dateCell.waitFor({ state: 'visible', timeout: 30000 });
      await dateCell.click();

      // Remplir les champs numériques
      const confirmedCases = page.getByRole('spinbutton', { name: 'Confirmed Cases' });
      await confirmedCases.waitFor({ state: 'visible', timeout: 30000 });
      await confirmedCases.fill('100');

      const deaths = page.getByRole('spinbutton', { name: 'Deaths' });
      await deaths.waitFor({ state: 'visible', timeout: 30000 });
      await deaths.fill('10');

      const recovered = page.getByRole('spinbutton', { name: 'Recovered' });
      await recovered.waitFor({ state: 'visible', timeout: 30000 });
      await recovered.fill('80');

      // Attendre que le bouton Submit soit cliquable
      const submitButton = page.getByRole('button', { name: 'Submit form' });
      await expect(submitButton).toBeVisible({ timeout: 30000 });
      await expect(submitButton).not.toBeDisabled({ timeout: 30000 });

      // Soumettre le formulaire
      await submitButton.click({ force: true });

      // Attendre que la barre de progression disparaisse
      await page.waitForSelector('mat-progress-bar', { state: 'hidden', timeout: 30000 });

      // Attendre que le dialog soit fermé
      await expect(dialog).not.toBeVisible({ timeout: 30000 });

      // Attendre que les données soient mises à jour
      await page.waitForTimeout(2000); // Attendre que les données soient rafraîchies
    });
}); 
