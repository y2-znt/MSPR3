import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, finalize, map, tap } from 'rxjs/operators';

export interface Language {
  code: string;
  name: string;
  flag: string;
}

@Injectable({
  providedIn: 'root',
})
export class TranslationService {
  private currentLanguageSubject = new BehaviorSubject<string>('fr');
  public currentLanguage$ = this.currentLanguageSubject.asObservable();

  private readonly STORAGE_KEY = 'preferred-language';
  private readonly DEFAULT_LANGUAGE = 'fr';

  private readonly supportedLanguages: Language[] = [
    { code: 'fr', name: 'Français', flag: '🇫🇷' },
    { code: 'en', name: 'English', flag: '🇺🇸' },
    { code: 'es', name: 'Español', flag: '🇪🇸' },
    { code: 'de', name: 'Deutsch', flag: '🇩🇪' },
    { code: 'it', name: 'Italiano', flag: '🇮🇹' },
  ];

  // Store loaded translations
  private translations: { [lang: string]: { [key: string]: any } } = {};
  private loadedLanguages: Set<string> = new Set();
  private isInitialized = false;

  constructor(private http: HttpClient) {
    this.initializeLanguage();
  }

  /**
   * Initialize the translation service with saved or browser language
   */
  public initializeLanguage(): void {
    if (this.isInitialized) {
      return;
    }

    // Get the saved language or the browser language
    const savedLanguage = this.getSavedLanguage();
    const browserLanguage = this.getBrowserLanguage();
    const initialLanguage =
      savedLanguage || browserLanguage || this.DEFAULT_LANGUAGE;

    // Load the translations and initialize the language
    this.loadTranslations(initialLanguage)
      .pipe(
        tap((translations) => {
          this.translations[initialLanguage] = translations;
          this.loadedLanguages.add(initialLanguage);
          this.currentLanguageSubject.next(initialLanguage);
          this.saveLanguagePreference(initialLanguage);
        }),
        finalize(() => {
          this.isInitialized = true;
        })
      )
      .subscribe();
  }

  /**
   * Get the saved language from localStorage
   */
  private getSavedLanguage(): string | null {
    try {
      return localStorage.getItem(this.STORAGE_KEY);
    } catch (error) {
      console.warn(
        'Unable to access localStorage for language preference:',
        error
      );
      return null;
    }
  }

  /**
   * Detect browser language and map to supported language
   */
  private getBrowserLanguage(): string {
    const browserLang = navigator.language.slice(0, 2);
    return this.supportedLanguages.some((lang) => lang.code === browserLang)
      ? browserLang
      : this.DEFAULT_LANGUAGE;
  }

  /**
   * Set the current language and load translations
   */
  public setLanguage(languageCode: string): void {
    if (!this.isSupportedLanguage(languageCode)) {
      console.warn(
        `Language ${languageCode} is not supported. Falling back to ${this.DEFAULT_LANGUAGE}`
      );
      languageCode = this.DEFAULT_LANGUAGE;
    }

    this.loadTranslations(languageCode)
      .pipe(
        tap((translations) => {
          this.translations[languageCode] = translations;
          this.loadedLanguages.add(languageCode);
          this.currentLanguageSubject.next(languageCode);
          this.saveLanguagePreference(languageCode);
        })
      )
      .subscribe();
  }

  /**
   * Load translations from JSON file
   */
  private loadTranslations(languageCode: string): Observable<any> {
    if (this.loadedLanguages.has(languageCode)) {
      return of(this.translations[languageCode]);
    }

    return this.http.get(`/assets/i18n/${languageCode}.json`).pipe(
      catchError((error) => {
        console.error(
          `Failed to load translations for ${languageCode}:`,
          error
        );
        // Fallback to default language if available, otherwise return empty object
        if (
          languageCode !== this.DEFAULT_LANGUAGE &&
          this.loadedLanguages.has(this.DEFAULT_LANGUAGE)
        ) {
          return of(this.translations[this.DEFAULT_LANGUAGE]);
        }
        return of({});
      })
    );
  }

  /**
   * Save language preference to localStorage
   */
  private saveLanguagePreference(languageCode: string): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, languageCode);
    } catch (error) {
      console.warn(
        'Unable to save language preference to localStorage:',
        error
      );
    }
  }

  /**
   * Check if language is supported
   */
  private isSupportedLanguage(languageCode: string): boolean {
    return this.supportedLanguages.some((lang) => lang.code === languageCode);
  }

  /**
   * Get current language code
   */
  public getCurrentLanguage(): string {
    return this.currentLanguageSubject.value;
  }

  /**
   * Get all supported languages
   */
  public getSupportedLanguages(): Language[] {
    return [...this.supportedLanguages];
  }

  /**
   * Get language details by code
   */
  public getLanguageByCode(code: string): Language | undefined {
    return this.supportedLanguages.find((lang) => lang.code === code);
  }

  /**
   * Get translation for a key
   */
  public getTranslation(key: string, params?: any): Observable<string> {
    const currentLang = this.getCurrentLanguage();

    if (!this.loadedLanguages.has(currentLang)) {
      return this.loadTranslations(currentLang).pipe(
        map(() => this.getTranslationValue(key, currentLang))
      );
    }

    return of(this.getTranslationValue(key, currentLang));
  }

  /**
   * Get instant translation for a key
   */
  public getInstantTranslation(key: string, params?: any): string {
    const currentLang = this.getCurrentLanguage();
    return this.getTranslationValue(key, currentLang);
  }

  /**
   * Get translation value from loaded translations
   */
  private getTranslationValue(key: string, languageCode: string): string {
    const langTranslations = this.translations[languageCode];
    if (!langTranslations) {
      return key;
    }

    // Support nested keys like 'app.title'
    const keys = key.split('.');
    let value: any = langTranslations;

    for (const k of keys) {
      if (value && typeof value === 'object' && k in value) {
        value = value[k];
      } else {
        return key; // Return original key if not found
      }
    }

    return typeof value === 'string' ? value : key;
  }

  /**
   * Check if translations are loaded for current language
   */
  public isTranslationLoaded(): boolean {
    return this.loadedLanguages.has(this.getCurrentLanguage());
  }

  /**
   * Reload translations for current language
   */
  public reloadTranslations(): Observable<any> {
    const currentLang = this.getCurrentLanguage();
    this.loadedLanguages.delete(currentLang);
    delete this.translations[currentLang];
    return this.loadTranslations(currentLang);
  }
}
