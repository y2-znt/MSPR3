import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { Country } from '../../models/country.model';
import { Page } from '../../models/pagination.model';
import { OrderByAlphaPipe } from '../../pipes/order-by-alpha.pipe';
import { CountryService } from '../../services/country.service';
import { OverviewComponent } from '../tabs/overview/overview.component';
import { DiseaseCaseService } from '../../services/disease-case.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  providers: [provideNativeDateAdapter(), CountryService, DiseaseCaseService],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    MatCardModule,
    MatIconModule,
    MatButtonToggleModule,
    MatSelectModule,
    MatTabsModule,
    OrderByAlphaPipe,
    OverviewComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit, OnDestroy {
  // Form Controls
  dateRange = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  countriesControl = new FormControl<Country[]>([]);
  countries: Country[] = [];
  currentPage = 0;
  pageSize = 250;

  // KPI Data
  totalCases: number = 0;
  totalDeaths: number = 0;
  mortalityRate: number = 0;
  totalRecoveries: number = 0;
  recoveryRate: number = 0;
  totalTests: number = 0;
  // totalConfirmed: number = 0;
  // totalDeaths: number = 0;
  // totalRecovered: number = 0;
  // totalTests: number = 0;

  constructor(private countryService: CountryService,
    private diseaseCaseService: DiseaseCaseService,
  ) {}

  ngOnInit(): void {
    this.loadCountries();
    this.getAllDiseasesCases(); // ➡️ Appel de la méthode pour récupérer les cas de maladies
  }

  public getAllDiseasesCases(): void {
    const allCases: any[] = [];
    let page = 0;
  
    const fetchPage = () => {
      console.log(`📄 Récupération de la page ${page}`);
  
      this.diseaseCaseService.getAllDiseaseCases(page, this.pageSize).subscribe({
        next: (res: any) => {
          console.log(`📥 Page ${page} reçue`, res);
  
          if (!res.content || !Array.isArray(res.content)) {
            console.warn('⚠️ Structure inattendue, pas de "content" dans la réponse.');
            return;
          }
  
          allCases.push(...res.content);
          console.log(`📊 Cas cumulés après page ${page}: ${allCases.length}`);
  
          if (!res.last) {
            page++;
            fetchPage(); // ➡️ Récupération de la page suivante
          } else {
            console.log('✅ Tous les cas récupérés:', allCases);
  
            // 🔢 Calculs
            this.totalCases = allCases.reduce((sum, item) => sum + item.confirmedCases, 0);
            this.totalDeaths = allCases.reduce((sum, item) => sum + item.deaths, 0);
            this.totalRecoveries = allCases.reduce((sum, item) => sum + item.recovered, 0);
  
            this.mortalityRate = this.totalCases ? +(this.totalDeaths / this.totalCases * 100).toFixed(2) : 0;
            this.recoveryRate = this.totalCases ? +(this.totalRecoveries / this.totalCases * 100).toFixed(2) : 0;
  
            console.log('🧮 Total Confirmés:', this.totalCases);
            console.log('⚰️ Total Décès:', this.totalDeaths);
            console.log('💪 Total Rétablis:', this.totalRecoveries);
            console.log('📈 Taux mortalité (%):', this.mortalityRate);
            console.log('📈 Taux guérison (%):', this.recoveryRate);
          }
        },
        error: (err) => {
          console.error("❌ Erreur lors de la récupération des données:", err);
        }
      });
    };
  
    fetchPage(); // 🚀 Démarre la récupération
  }
  

  loadCountries(): void {
    this.countryService
      .getAllCountries(this.currentPage, this.pageSize)
      .subscribe(
        (page: Page<Country>) => {
          this.countries = page.content;
        },
        (error) => {
          console.error('Error loading countries:', error);
        }
      );
  }

  ngOnDestroy(): void {
    this.countries = [];
  }
}
