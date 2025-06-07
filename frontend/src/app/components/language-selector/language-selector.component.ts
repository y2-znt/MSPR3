import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { Subject, takeUntil } from 'rxjs';

import { TranslationService, Language } from '../../services/translation.service';
import { TranslatePipe } from '../../pipes/translate.pipe';

@Component({
  selector: 'app-language-selector',
  standalone: true,
  imports: [
    CommonModule,
    MatSelectModule,
    MatFormFieldModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    TranslatePipe
  ],
  templateUrl: './language-selector.component.html',
  styleUrls: ['./language-selector.component.scss']
})
export class LanguageSelectorComponent implements OnInit, OnDestroy {
  supportedLanguages: Language[] = [];
  currentLanguage: string = 'fr';
  currentLanguageDetails: Language | undefined;
  
  private destroy$ = new Subject<void>();

  constructor(private translationService: TranslationService) {}

  ngOnInit(): void {
    this.supportedLanguages = this.translationService.getSupportedLanguages();
    
    // S'abonner aux changements de langue
    this.translationService.currentLanguage$
      .pipe(takeUntil(this.destroy$))
      .subscribe(language => {
        this.currentLanguage = language;
        this.currentLanguageDetails = this.translationService.getLanguageByCode(language);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Change la langue sélectionnée
   */
  onLanguageChange(languageCode: string): void {
    this.translationService.setLanguage(languageCode);
  }

  /**
   * Récupère les détails d'une langue
   */
  getLanguageDetails(code: string): Language | undefined {
    return this.translationService.getLanguageByCode(code);
  }
}
