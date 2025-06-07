import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Country } from '../../../models/country.model';
import { CountryService } from '../../../services/country.service';
import {
  CountryData,
  CovidDataService,
  CovidStats,
} from '../../../services/covid-data.service';
import { DiseaseCaseService } from '../../../services/disease-case.service';
import { EditDialogComponent } from '../../edit-dialog/edit-dialog.component';
import { TranslatePipe } from '../../../pipes/translate.pipe';

@Component({
  selector: 'app-countries',
  standalone: true,  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    TranslatePipe,
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

  diseaseName: string = 'COVID-19';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private covidDataService: CovidDataService,
    private countryService: CountryService,
    private diseaseCaseService: DiseaseCaseService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    if (this.countries && this.countries.length > 0) {
      this.loadCountriesStats();
    }
  }

  ngAfterViewInit(): void {
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

    const requests = this.countries.map((country) => {
      return this.countryService.getCountriesStats(country.name);
    });

    forkJoin(requests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (results) => {
          const countryData: CountryData[] = this.countries.map(
            (country, index) => {
              const countryResults = results[index];

              let latestStats = null;
              if (countryResults && countryResults.length > 0) {
                latestStats = countryResults[countryResults.length - 1];
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
                id: latestStats?.id || country.id,
                date: latestStats?.date || this.formatDate(new Date()),
              };
            }
          );

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
        const index = this.dataSource.data.findIndex(
          (item) => item.id === result.id
        );
        if (index !== -1) {
          const updatedData = [...this.dataSource.data];
          updatedData[index] = {
            ...updatedData[index],
            totalCases: result.confirmedCases,
            deaths: result.deaths,
            recovered: result.recovered,
            mortalityRate: this.calculateRate(
              result.deaths,
              result.confirmedCases
            ),
            recoveryRate: this.calculateRate(
              result.recovered,
              result.confirmedCases
            ),
          };
          this.dataSource.data = updatedData;
          this.cdr.detectChanges();
        }
      }
    });
  }

  confirmDelete(element: CountryData): void {
    if (!element.id) {
      alert(
        `Cannot delete a case for ${element.country} without an identifier.`
      );
      return;
    }

    if (
      confirm(
        `Are you sure you want to delete the case for ${element.country}?`
      )
    ) {
      this.removeElementFromTable(element);
      this.diseaseCaseService.deleteDiseaseCase(element.id).subscribe({
        next: (response: any) => {
          console.log('Case deleted successfully:', response);
        },
        error: (error: any) => {
          console.error('Error deleting case:', error);
        },
      });
    }
  }

  /**
   * Removes an element from the data table
   */
  private removeElementFromTable(element: CountryData): void {
    const index = this.dataSource.data.findIndex(
      (item) =>
        item.id === element.id ||
        (item.country === element.country && item.date === element.date)
    );

    console.log('Index found in table:', index);

    if (index !== -1) {
      console.log('Removing element from table at index:', index);
      // Remove the element from the array
      const updatedData = [...this.dataSource.data];
      updatedData.splice(index, 1);
      this.dataSource.data = updatedData;
      console.log('Table updated, new size:', this.dataSource.data.length);

      // Force a view update
      this.cdr.detectChanges();
      console.log('View updated via ChangeDetectorRef');
    } else {
      console.log('Element not found in table');
    }
  }

  refreshData(): void {
    console.log('Data updated, reloading required');
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
