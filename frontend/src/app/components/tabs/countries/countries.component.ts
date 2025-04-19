import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild, ChangeDetectorRef, Input, SimpleChanges } from '@angular/core';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';
import { CountryData, CovidDataService, CovidStats } from '../../../services/covid-data.service';
import { Subject } from 'rxjs';
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
  @Input() mortalityRate = 0;
  @Input() recoveryRate = 0;
  @Input() diseaseName = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  
  public displayedColumns: string[] = ['country', 'totalCases', 'deaths', 'recovered', 'mortalityRate', 'recoveryRate'];
  public dataSource: MatTableDataSource<CountryData> = new MatTableDataSource<CountryData>();
  private destroy$ = new Subject<void>();
  private countriesData: CountryData[] = [];
  public error: string | null = null;

  constructor(
    private covidDataService: CovidDataService,
    private cdr: ChangeDetectorRef
  ) {}
  
  ngOnInit(): void {
    // Assurer que nous avons des données initiales
    this.dataSource.data = this.countriesData
    
    // S'abonner aux mises à jour des statistiques globales COVID
    this.covidDataService.covidStats$
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => {
        if (stats) {
          console.log('COVID stats reçues dans CountriesComponent', stats);
          this.updateCovidStats(stats);
        }
      });

    this.covidDataService.countriesData$
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (data) => {
        console.log('Données pays reçues:', data);
        if (data && data.length > 0) {
          this.countriesData = data; 
          this.dataSource.data = this.countriesData;
          this.error = null;
          console.log('Nombre de pays chargés:', data.length);
          // Afficher le premier pays pour vérifier la structure
          console.log('Premier pays:', data[0]);
        } else {
          this.error = 'Aucune donnée disponible';
          this.countriesData = [];
          console.log('Aucune donnée reçue');
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des données:', err);
        this.error = 'Erreur lors du chargement des données';
        this.isLoading = false;
        this.countriesData = [];
        this.cdr.detectChanges();
      }
    });
    
    // Vérifier si les données sont déjà disponibles
    const currentStats = this.covidDataService.getCovidStats();
    if (currentStats) {
      console.log('Utilisation des statistiques COVID existantes');
      this.updateCovidStats(currentStats);
    }
    
    const countriesData = this.covidDataService.getCountriesData();
    if (countriesData && countriesData.length > 0) {
      console.log(`Utilisation des données existantes pour ${countriesData.length} pays`);
      this.dataSource.data = countriesData;
      this.isLoading = false;
    }
    
    // Forcer la détection des changements
    this.cdr.detectChanges();
  }

  ngAfterViewInit(): void {
    console.log('CountriesComponent: ngAfterViewInit');
    
    // Configurer le paginator et le sort dès que possible
    setTimeout(() => {
      console.log('Paginator:', this.paginator);
      console.log('Sort:', this.sort);
      
      if (this.paginator) {
        this.dataSource.paginator = this.paginator;
      }
      
      if (this.sort) {
        this.dataSource.sort = this.sort;
      }
      
      console.log('DataSource data length:', this.dataSource.data.length);
    }, 100);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['countries'] && changes['countries'].currentValue) {
      console.log('Countries data received:', this.countries);
      // Mettre à jour le tableau avec les nouvelles données
      this.updateTableData();
    }
  }

  private updateTableData() {
    if (this.countries.length > 0) {
      const countryData: CountryData[] = this.countries.map(country => ({
        country: country.name,
        totalCases: 0,
        deaths: 0,
        recovered: 0,
        mortalityRate: 0,
        recoveryRate: 0
      }));
      this.dataSource.data = countryData;
      console.log('Table data updated with', this.dataSource.data.length, 'countries');
    }
  }

  updateCovidStats(stats: CovidStats): void {
    this.diseaseName = stats.diseaseName;
    this.totalCases = stats.totalCases;
    this.totalDeaths = stats.totalDeaths;
    this.totalRecoveries = stats.totalRecoveries;
    // this.mortalityRate = stats.mortalityRate;
    // this.recoveryRate = stats.recoveryRate;
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
