import {
  AfterViewInit,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
  ChangeDetectorRef,
  Input,
  SimpleChanges,
} from '@angular/core';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import {
  CountryData,
  CovidDataService,
  CovidStats,
} from '../../../services/covid-data.service';
import { CountryService } from '../../../services/country.service';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Country } from '../../../models/country.model';
import { EditDialogComponent } from '../../edit-dialog/edit-dialog.component';

@Component({
  selector: 'app-countries',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './countries.component.html',
  styleUrl: './countries.component.scss',
})
export class CountriesComponent implements AfterViewInit, OnInit, OnDestroy {
  @Input() countries: Country[] = [];
  @Input() isLoading = false;
  @Input() totalCases = 0;
  @Input() totalDeaths = 0;
  @Input() totalRecoveries = 0;

  displayedColumns: string[] = [
    'country',
    'totalCases',
    'deaths',
    'recovered',
    'mortalityRate',
    'recoveryRate',
    'actions',
  ];
  dataSource: MatTableDataSource<CountryData> =
    new MatTableDataSource<CountryData>();
  private destroy$ = new Subject<void>();
  private countriesData: CountryData[] = [];
  public error: string | null = null;

  // Demonstration COVID statistics (in case real data is not available)
  diseaseName: string = 'COVID-19';
  mortalityRate: number = 5.24;
  recoveryRate: number = 46.88;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private covidDataService: CovidDataService,
    private countryService: CountryService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    console.log('CountriesComponent: ngOnInit');

    if (this.countries && this.countries.length > 0) {
      this.loadCountriesStats();
    }
  }

  ngAfterViewInit(): void {
    console.log('CountriesComponent: ngAfterViewInit');

    if (this.paginator) {
      this.dataSource.paginator = this.paginator;
    }

    if (this.sort) {
      this.dataSource.sort = this.sort;
    }

    this.dataSource.sortingDataAccessor = (item, property) => {
      switch (property) {
        case 'totalCases':
          return item.totalCases;
        case 'deaths':
          return item.deaths;
        case 'recovered':
          return item.recovered;
        case 'mortalityRate':
          return item.mortalityRate;
        case 'recoveryRate':
          return item.recoveryRate;
        default:
          return item[property as keyof CountryData] as string;
      }
    };
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['countries'] && changes['countries'].currentValue) {
      console.log('Countries changed:', this.countries);
      this.loadCountriesStats();
    }
  }

  updateCovidStats(stats: CovidStats): void {
    this.diseaseName = stats.diseaseName;
    this.totalCases = stats.totalCases;
    this.totalDeaths = stats.totalDeaths;
    this.totalRecoveries = stats.totalRecoveries;
  }

  private loadCountriesStats(): void {
    if (!this.countries || this.countries.length === 0) {
      console.error('No countries to load');
      return;
    }

    this.isLoading = true;
    console.log(`Loading stats for ${this.countries.length} countries`);

    const requests = this.countries.map((country) => {
      if (this.countries.indexOf(country) < 3) {
        console.log(`Request for ${country.name}`);
      }
      return this.countryService.getCountriesStats(country.name);
    });

    forkJoin(requests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (results) => {
          console.log(`Received results for ${results.length} countries`);

          const countryData: CountryData[] = this.countries.map(
            (country, index) => {
              const countryResults = results[index];

              let latestStats = null;
              if (countryResults && countryResults.length > 0) {
                latestStats = countryResults[countryResults.length - 1];
                if (index < 3) {
                  console.log(
                    `Last stats for ${country.name}:`,
                    latestStats
                  );
                }
              }

              return {
                country: country.name,
                totalCases: latestStats?.confirmedCases || 0,
                deaths: latestStats?.deaths || 0,
                recovered: latestStats?.recovered || 0,
                mortalityRate: this.calculateRate(
                  latestStats?.deaths,
                  latestStats?.confirmedCases
                ),
                recoveryRate: this.calculateRate(
                  latestStats?.recovered,
                  latestStats?.confirmedCases
                ),
                // Ajouter l'ID et la date à partir des statistiques, ou utiliser des valeurs par défaut
                id: latestStats?.id || this.generateTempId(country.name),
                date: latestStats?.date || this.formatDate(new Date())
              };
            }
          );

          console.log(`Generated data for ${countryData.length} countries`);
          this.dataSource.data = countryData;
          this.countriesData = countryData;
          this.isLoading = false;
          this.error = null;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading stats:', error);
          this.isLoading = false;
          this.error = 'Error loading data';
          this.cdr.detectChanges();
        },
      });
  }

  private calculateRate(numerator: number, denominator: number): number {
    const rate = denominator > 0 ? (numerator / denominator) * 100 : 0;
    return parseFloat(rate.toFixed(2));
  }

  private generateTempId(countryName: string): string {
    return `temp-${countryName}-${Date.now()}`;
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  openEditDialog(element: CountryData): void {
    const dialogRef = this.dialog.open(EditDialogComponent, {
      width: '500px',
      data: {
        id: element.id,
        country: element.country,
        date: element.date,
        confirmedCases: element.totalCases,
        deaths: element.deaths,
        recovered: element.recovered,
        countries: this.countries,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.success) {
        // Refresh data after update
        this.refreshData();
      }
    });
  }

  refreshData(): void {
    // You will need to implement this method to reload data from your service
    console.log('Data updated, reloading required');
    // Example: this.loadCountryData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
