import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
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
import { CountryData, CovidDataService } from '../../services/covid-data.service';
import { DiseaseCaseService } from '../../services/disease-case.service';
import { OverviewComponent } from '../tabs/overview/overview.component';
import { CountriesComponent } from '../tabs/countries/countries.component';

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
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit, OnDestroy {
  isLoading: boolean = false;

  // Form Controls
  dateRange = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  countriesControl = new FormControl<Country[]>([]);
  countries: Country[] = [];
  diseaseName: string = '';
  currentPage = 0;
  pageSize = 250;

  // KPI Data
  totalCases: number = 0;
  totalDeaths: number = 0;
  mortalityRate: number = 0;
  totalRecoveries: number = 0;
  recoveryRate: number = 0;
  totalTests: number = 0;

  constructor(
    private countryService: CountryService,
    private diseaseCaseService: DiseaseCaseService,
    private covidDataService: CovidDataService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCountries();
    this.getAllDiseasesCases();
  }

  get kpiCards() {
    return [
      {
        label: 'Cas Totaux',
        icon: 'people',
        subtitle: 'Cas confirmés de COVID-19 dans le monde',
        value: this.totalCases,
      },
      {
        label: 'Décès Totaux',
        icon: 'warning',
        subtitle: `Taux de mortalité: ${this.mortalityRate}%`,
        value: this.totalDeaths,
      },
      {
        label: 'Guérisons',
        icon: 'health_and_safety',
        subtitle: `Taux de guérison: ${this.recoveryRate}%`,
        value: this.totalRecoveries,
      },
    ];
  }

  public getAllDiseasesCases(): void {
    const allCases: any[] = [];
    let page = 0;

    const fetchPage = () => {
      console.log(`📄 Récupération de la page ${page}`);

      this.diseaseCaseService
        .getAllDiseaseCases(page, this.pageSize)
        .subscribe({
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
              fetchPage();
            } else {
              console.log('✅ Tous les cas récupérés:', allCases);

              this.diseaseName = allCases[0]?.name || 'COVID-19';
              console.log('🦠 Maladie:', this.diseaseName);

              // Calcul des statistiques globales
              this.totalCases = allCases.reduce(
                (sum, item) => sum + (item.confirmedCases || 0),
                0
              );
              this.totalDeaths = allCases.reduce(
                (sum, item) => sum + (item.deaths || 0),
                0
              );
              this.totalRecoveries = allCases.reduce(
                (sum, item) => sum + (item.recovered || 0),
                0
              );

              this.mortalityRate = this.totalCases
                ? +((this.totalDeaths / this.totalCases) * 100).toFixed(2)
                : 0;
              this.recoveryRate = this.totalCases
                ? +((this.totalRecoveries / this.totalCases) * 100).toFixed(2)
                : 0;

              console.log('🧮 Total Confirmés:', this.totalCases);
              console.log('⚰️ Total Décès:', this.totalDeaths);
              console.log('💪 Total Rétablis:', this.totalRecoveries);
              console.log('📈 Taux mortalité (%):', this.mortalityRate);
              console.log('📈 Taux guérison (%):', this.recoveryRate);
              
              // Partager les statistiques globales via le service
              this.covidDataService.updateCovidStats({
                diseaseName: this.diseaseName,
                totalCases: this.totalCases,
                totalDeaths: this.totalDeaths,
                totalRecoveries: this.totalRecoveries,
                mortalityRate: this.mortalityRate,
                recoveryRate: this.recoveryRate
              });
              
              // Regroupement des données par pays
              const countryMap = new Map<string, CountryData>();
              
              allCases.forEach(item => {
                const countryName = item.country?.name || 'Unknown';
                
                if (!countryMap.has(countryName)) {
                  countryMap.set(countryName, {
                    country: countryName,
                    totalCases: 0,
                    deaths: 0,
                    recovered: 0,
                    mortalityRate: 0,
                    recoveryRate: 0
                  });
                }
                
                const countryData = countryMap.get(countryName)!;
                countryData.totalCases += item.confirmedCases || 0;
                countryData.deaths += item.deaths || 0;
                countryData.recovered += item.recovered || 0;
              });
              
              // Calcul des taux pour chaque pays
              countryMap.forEach(data => {
                data.mortalityRate = data.totalCases > 0 ? +(data.deaths / data.totalCases * 100).toFixed(2) : 0;
                data.recoveryRate = data.totalCases > 0 ? +(data.recovered / data.totalCases * 100).toFixed(2) : 0;
              });
              
              // Partager les données par pays via le service
              this.covidDataService.updateCountriesData(Array.from(countryMap.values()));

              // Forcer la détection des changements après la mise à jour des données
              this.cdr.detectChanges();
            }
          },
          error: (err) => {
            console.error('❌ Erreur lors de la récupération des données:', err);
            this.cdr.detectChanges();
          },
        });
    };

    fetchPage();
  }

  // loadCountries(): void {
  //   this.countryService
  //     .getAllCountries(this.currentPage, this.pageSize)
  //     .subscribe(
  //       (page: Page<Country>) => {
  //         this.countries = page.content;
  //         console.log('Countries loaded:', this.countries);
  //       },
  //       (error) => {
  //         console.error('Error loading countries:', error);
  //       }
  //     );
  // }

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
  
  ngOnDestroy(): void {
    this.countries = [];
  }
}
