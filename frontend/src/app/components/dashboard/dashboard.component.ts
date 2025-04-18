import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
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
import { TotalKpiDto } from '../../models/diseaseCase.model';
import { Page } from '../../models/pagination.model';
import { OrderByAlphaPipe } from '../../pipes/order-by-alpha.pipe';
import { CountryService } from '../../services/country.service';
import { DiseaseCaseService } from '../../services/disease-case.service';
import { OverviewComponent } from '../tabs/overview/overview.component';
import { CountriesComponent } from '../tabs/countries/countries.component';
import { CountryData } from '../../services/covid-data.service';
import { ChangeDetectorRef } from '@angular/core';
import { CovidDataService } from '../../services/covid-data.service';

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
    CountriesComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;

  // Form Controls
  dateRange = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  countriesControl = new FormControl<Country[]>([]);
  countries: Country[] = [];
  diseaseName: string = 'COVID-19';
  currentPage = 0;
  pageSize = 250;

  // KPI Data
  totalCases: number = 0;
  totalDeaths: number = 0;
  mortalityRate: number = 0;
  totalRecoveries: number = 0;
  recoveryRate: number = 0;

  constructor(
    private countryService: CountryService,
    private covidDataService: CovidDataService,
    private cdr: ChangeDetectorRef,
    private diseaseCaseService: DiseaseCaseService
  ) {}

  ngOnInit(): void {
    // Changer l'ordre des appels
    this.loadCountries(); // D'abord charger la liste des pays
    this.loadKpiData(); // Ensuite charger les données KPI
  }

  ngOnDestroy(): void {}

  get kpiCards() {
    return [
      {
        label: 'Cas Totaux',
        icon: 'people',
        subtitle: `Cas confirmés de ${this.diseaseName} dans le monde`,
        value: this.totalCases,
      },
      {
        label: 'Décès Totaux',
        icon: 'warning',
        subtitle: `Taux de mortalité: ${this.mortalityRate.toFixed(2)}%`,
        value: this.totalDeaths,
      },
      {
        label: 'Guérisons',
        icon: 'health_and_safety',
        subtitle: `Taux de guérison: ${this.recoveryRate.toFixed(2)}%`,
        value: this.totalRecoveries,
      },
    ];
  }

  loadCountries(): void {
    console.log('🌍 Début du chargement des pays...');
    this.isLoading = true;
    
    this.countryService
      .getAllCountries(this.currentPage, this.pageSize)
      .subscribe({
        next: (page: Page<Country>) => {
          console.log('✅ Pays chargés avec succès:', {
            totalElements: page.content.length,
            firstCountry: page.content[0],
            lastCountry: page.content[page.content.length - 1]
          });
          
          this.countries = page.content;
          
          // Ajout : Mettre à jour les données dans le service
          const countryData: CountryData[] = this.countries.map(country => ({
            country: country.name,
            totalCases: 0,
            deaths: 0,
            recovered: 0,
            mortalityRate: 0,
            recoveryRate: 0
          }));
          
          // Mettre à jour le service avec la liste initiale des pays
          this.covidDataService.updateCountriesData(countryData);
          console.log('📤 Données pays mises à jour dans le service:', countryData);

          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('❌ Erreur lors du chargement des pays:', error);
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        complete: () => {
          console.log('🏁 Chargement des pays terminé');
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
  }

  loadKpiData(): void {
    this.diseaseCaseService.getTotalKpi().subscribe(
      (kpi: TotalKpiDto) => {
        this.totalCases = kpi.totalCases;
        this.totalDeaths = kpi.totalDeaths;
        this.mortalityRate = kpi.mortalityRate;
        this.totalRecoveries = kpi.totalRecovered;
        this.recoveryRate = kpi.recoveryRate;
        this.isLoading = false;
      },
      (error) => {
        console.error('Error loading KPI data:', error);
        this.isLoading = false;
      }
    );
  }
}
