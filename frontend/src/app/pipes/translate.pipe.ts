import { Pipe, PipeTransform, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { TranslationService } from '../services/translation.service';
import { Subscription } from 'rxjs';

@Pipe({
  name: 'translate',
  standalone: true,
  pure: false // Make it impure to react to language changes
})
export class TranslatePipe implements PipeTransform, OnDestroy {
  private subscription?: Subscription;
  private lastValue: string = '';
  private lastKey: string = '';
  private currentLanguage: string = '';

  constructor(private translationService: TranslationService) {
    // Subscribe to language changes to invalidate cache
    this.subscription = this.translationService.currentLanguage$.subscribe((lang) => {
      if (this.currentLanguage !== lang) {
        this.currentLanguage = lang;
        this.lastValue = ''; // Clear cache when language changes
        this.lastKey = '';
      }
    });
  }

  transform(key: string, params?: any): string {
    if (!key) return '';
    
    const currentLang = this.translationService.getCurrentLanguage();
    
    // If key changed, language changed, or no cached value, get new translation
    if (key !== this.lastKey || this.currentLanguage !== currentLang || !this.lastValue) {
      this.lastKey = key;
      this.currentLanguage = currentLang;
      this.lastValue = this.translationService.getInstantTranslation(key, params);
    }
    
    return this.lastValue;
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
