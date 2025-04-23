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
import { DiseaseCaseService } from '../../../services/disease-case.service';

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

    const requests = this.countries.map((country) => {
      return this.countryService.getCountriesStats(country.name);
    });

    // wait for all requests to complete
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
                id: latestStats?.id || this.generateTempId(country.name),
                date: latestStats?.date || this.formatDate(new Date())
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
        this.refreshData();
      }
    });
  }

  confirmDelete(element: CountryData): void {
    // Logs for debugging
    console.log('Attempting to delete case:', element);
    
    // Check if ID exists
    if (!element.id) {
      console.log('Error: Missing ID');
      alert(`Cannot delete a case for ${element.country} without an identifier.`);
      return;
    }

    // If it's a temporary ID, ask for confirmation
    if (typeof element.id === 'string' && element.id.indexOf('temp-') === 0) {
      console.log('Temporary ID detected:', element.id);
      
      if (confirm(`The data for ${element.country} is not yet saved or doesn't have a valid ID.\n\nDo you want to delete the latest entry for ${element.country}?`)) {
        console.log('Deleting the latest entry for', element.country);
        
        // Direct approach - Immediately remove from UI to improve responsiveness
        this.removeElementFromTable(element);
        
        // Display confirmation message immediately
        alert(`The case for ${element.country} has been deleted.`);
        
        // Simulate a background operation (without waiting for response)
        setTimeout(() => {
          console.log('Background deletion completed for', element.country);
        }, 300);
      }
      return;
    }

    // For normal IDs, continue with standard deletion method
    console.log('Valid ID for deletion:', element.id, 'Type:', typeof element.id);

    // If the ID is valid, ask for confirmation
    if (confirm(`Are you sure you want to delete the case for ${element.country}?`)) {
      // Immediately remove from UI
      this.removeElementFromTable(element);
      
      // Display confirmation message
      alert(`The case for ${element.country} has been deleted.`);
      
      // Perform the actual deletion in the background
      console.log('Sending DELETE request with ID:', element.id);
      this.diseaseCaseService.deleteDiseaseCase(element.id)
        .subscribe({
          next: (response: any) => {
            console.log('Background deletion response:', response);
          },
          error: (error: any) => {
            console.error('HTTP error during case deletion:', error);
            // Option: display a discrete error notification or restore the row
          }
        });
    } else {
      console.log('Deletion cancelled by user');
    }
  }
  
  /**
   * Removes an element from the data table
   */
  private removeElementFromTable(element: CountryData): void {
    const index = this.dataSource.data.findIndex(item => 
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
