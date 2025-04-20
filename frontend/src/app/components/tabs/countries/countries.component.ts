import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild, ChangeDetectorRef, Input, SimpleChanges } from '@angular/core';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';
import { CountryData, CovidDataService, CovidStats } from '../../../services/covid-data.service';
import { CountryService } from '../../../services/country.service';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Country } from '../../../models/country.model';

@Component({
  selector: 'app-countries',
  standalone: true,
  imports: [CommonModule, MatFormFieldModule, MatInputModule, MatTableModule, MatSortModule, MatPaginatorModule],
  templateUrl: './countries.component.html',
  styleUrl: './countries.component.scss',
})
export class CountriesComponent implements AfterViewInit, OnInit, OnDestroy {
  @Input() countries: Country[] = [];
  @Input() isLoading = false;
  @Input() totalCases = 0;
  @Input() totalDeaths = 0;
  @Input() totalRecoveries = 0;

  displayedColumns: string[] = ['country', 'totalCases', 'deaths', 'recovered', 'mortalityRate', 'recoveryRate'];
  dataSource: MatTableDataSource<CountryData> = new MatTableDataSource<CountryData>();
  private destroy$ = new Subject<void>();
  private countriesData: CountryData[] = [];
  public error: string | null = null;

  // Statistiques COVID de démonstration (en cas où les données réelles ne sont pas disponibles)
  diseaseName: string = 'COVID-19';
  mortalityRate: number = 5.24;
  recoveryRate: number = 46.88;
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private covidDataService: CovidDataService,
    private countryService: CountryService,
    private cdr: ChangeDetectorRef
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
      switch(property) {
        case 'totalCases': return item.totalCases;
        case 'deaths': return item.deaths;
        case 'recovered': return item.recovered;
        case 'mortalityRate': return item.mortalityRate;
        case 'recoveryRate': return item.recoveryRate;
        default: return item[property as keyof CountryData] as string;
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
      console.error('Aucun pays à charger');
      return;
    }
    
    this.isLoading = true;
    console.log(`Chargement des stats pour ${this.countries.length} pays`);
    
    const requests = this.countries.map(country => {
      if (this.countries.indexOf(country) < 3) {
        console.log(`Requête pour ${country.name}`);
      }
      return this.countryService.getCountriesStats(country.name);
    });
    
    forkJoin(requests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (results) => {
          console.log(`Résultats reçus pour ${results.length} pays`);
          
          const countryData: CountryData[] = this.countries.map((country, index) => {
            const countryResults = results[index];
            
            let latestStats = null;
            if (countryResults && countryResults.length > 0) {
              latestStats = countryResults[countryResults.length - 1];
              if (index < 3) {
                console.log(`Dernière stats pour ${country.name}:`, latestStats);
              }
            }
            
            return {
              country: country.name,
              totalCases: latestStats?.confirmedCases || 0,
              deaths: latestStats?.deaths || 0,
              recovered: latestStats?.recovered || 0,
              mortalityRate: this.calculateRate(latestStats?.deaths, latestStats?.confirmedCases),
              recoveryRate: this.calculateRate(latestStats?.recovered, latestStats?.confirmedCases)
            };
          });
          
          console.log(`Données générées pour ${countryData.length} pays`);
          this.dataSource.data = countryData;
          this.countriesData = countryData;
          this.isLoading = false;
          this.error = null;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Erreur lors du chargement des stats:', error);
          this.isLoading = false;
          this.error = "Erreur lors du chargement des données";
          this.cdr.detectChanges();
        }
      });
  }

  private calculateRate(numerator: number, denominator: number): number {
    const rate = denominator > 0 ? (numerator / denominator) * 100 : 0;
    return parseFloat(rate.toFixed(2));
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
