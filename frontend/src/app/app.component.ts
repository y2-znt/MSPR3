import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslationService } from './services/translation.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  template: `
    <div *ngIf="isTranslationLoaded">
      <router-outlet></router-outlet>
    </div>
  `,
})
export class AppComponent implements OnInit {
  isTranslationLoaded = false;

  constructor(private translationService: TranslationService) {}

  ngOnInit() {
    this.translationService.initializeLanguage();

    // Subscribe to language changes to know when translations are loaded
    this.translationService.currentLanguage$.subscribe(() => {
      if (this.translationService.isTranslationLoaded()) {
        this.isTranslationLoaded = true;
      }
    });
  }
}
