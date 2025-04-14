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

@Component({
  selector: 'app-dashboard',
  standalone: true,
  providers: [provideNativeDateAdapter(), CountryService],
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
  totalCases: number = 675432198;
  totalDeaths: number = 6932408;
  mortalityRate: number = 1.03;
  totalRecoveries: number = 648392781;
  recoveryRate: number = 96.0;
  totalTests: number = 7023456789;

  constructor(private countryService: CountryService) {}

  ngOnInit(): void {
    this.loadCountries();
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
