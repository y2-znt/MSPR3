import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { DialogComponent } from '../../components/dialog/dialog.component';
import { Country } from '../../models/country.model';
import { TotalKpiDto } from '../../models/diseaseCase.model';
import { Page } from '../../models/pagination.model';
import { OrderByAlphaPipe } from '../../pipes/order-by-alpha.pipe';
import { CountryService } from '../../services/country.service';
import {
  CountryData,
  CovidDataService,
} from '../../services/covid-data.service';
import { DiseaseCaseService } from '../../services/disease-case.service';
import { CountriesComponent } from '../tabs/countries/countries.component';
import { OverviewComponent } from '../tabs/overview/overview.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  providers: [
    provideNativeDateAdapter(),
    CountryService,
    DiseaseCaseService,
    DialogComponent,
  ],
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
    MatButtonModule,
    MatDialogModule,
    OrderByAlphaPipe,
    OverviewComponent,
    CountriesComponent,
    DialogComponent,
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
    private diseaseCaseService: DiseaseCaseService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadCountries();
    this.loadKpiData();

    this.dateRange.valueChanges.subscribe((dateRange) => {
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy(): void {}

  get kpiCards() {
    return [
      {
        label: 'Total Cases',
        icon: 'people',
        subtitle: `Cases of ${this.diseaseName} in the world`,
        value: this.totalCases,
      },
      {
        label: 'Total Deaths',
        icon: 'warning',
        subtitle: `Mortality rate: ${this.mortalityRate.toFixed(2)}%`,
        value: this.totalDeaths,
      },
      {
        label: 'Recoveries',
        icon: 'health_and_safety',
        subtitle: `Recovery rate: ${this.recoveryRate.toFixed(2)}%`,
        value: this.totalRecoveries,
      },
    ];
  }

  loadCountries(): void {
    this.isLoading = true;

    this.countryService
      .getAllCountries(this.currentPage, this.pageSize)
      .subscribe({
        next: (page: Page<Country>) => {
          this.countries = page.content;

          // Add: Update the data in the service
          const countryData: CountryData[] = this.countries.map((country) => ({
            country: country.name,
            totalCases: 0,
            deaths: 0,
            recovered: 0,
            mortalityRate: 0,
            recoveryRate: 0,
          }));

          // Update the service with the initial list of countries
          this.covidDataService.updateCountriesData(countryData);
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading countries:', error);
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        complete: () => {
          this.isLoading = false;
          this.cdr.detectChanges();
        },
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

  get formattedDateRange(): { start: string | null; end: string | null } {
    const startValue = this.dateRange.get('start')?.value || null;
    const endValue = this.dateRange.get('end')?.value || null;

    const start = startValue ? this.formatDate(startValue) : null;
    const end = endValue ? this.formatDate(endValue) : null;

    return { start, end };
  }

  // Formated date YYYY-MM-DD
  private formatDate(date: Date | null): string | null {
    if (!date) return null;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  openAddCaseDialog(): void {
    const dialogRef = this.dialog.open(DialogComponent, {
      width: '500px',
      data: { countries: this.countries },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadCountries();
        this.loadKpiData();
      }
    });
  }
}
