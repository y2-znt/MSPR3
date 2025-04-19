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
    private diseaseCaseService: DiseaseCaseService
  ) {}

  ngOnInit(): void {
    this.loadCountries();
    this.loadKpiData();
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
