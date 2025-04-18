package mspr.backend.Runner;

import mspr.backend.BO.Disease;
import mspr.backend.Helpers.CacheHelper;
import mspr.backend.Repository.*;
import mspr.backend.Service.CsvImporter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataImportCovid19Runner implements CommandLineRunner {

    @Autowired private CovidCompleteService covidCompleteService;
    @Autowired private FullGroupedService fullGroupedService;
    @Autowired private UsaCountyService usaCountyService;
    @Autowired private WorldometerService worldometerService;

    @Autowired private CountryRepository countryRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private DiseaseCaseRepository diseaseCaseRepository;
    @Autowired private DiseaseRepository diseaseRepository;

    @Autowired private CacheHelper cacheHelper;

    @Override
    public void run(String... args) throws Exception {
        // Start the overall timer
        long importStartTime = System.currentTimeMillis();

        System.out.println("Suppressions des données existantes...");
        deleteAllDataInBatch();
        System.out.println("Suppression des données terminée.");

        // Prepare COVID-19 Disease entity in the database and add it to the cache
        Disease covid = diseaseRepository.findByName("COVID-19");
        if (covid == null) {
            covid = new Disease();
            covid.setName("COVID-19");
            covid = diseaseRepository.save(covid);
        }
        cacheHelper.addDiseaseToCache("COVID-19", covid);

        // Variables to hold durations and line counts for each import
        long startTime, endTime;
        long durationWorldometer = 0, durationCovidComplete = 0, durationFullGrouped = 0, durationUsaCounty = 0;
        int linesWorldometer = 0, linesCovidComplete = 0, linesFullGrouped = 0, linesUsaCounty = 0;

        System.out.println("** Début de l'import des données COVID **");

        // 1. Import Worldometer Data
        System.out.println("Import de Worldometer...");
        startTime = System.currentTimeMillis();
        linesWorldometer = worldometerService.importData();
        endTime = System.currentTimeMillis();
        durationWorldometer = endTime - startTime;
        System.out.println("Import de Worldometer terminé.");

        // 2. Import detailed global data (covid_19_clean_complete.csv)
        System.out.println("Import de covid_19_clean_complete...");
        startTime = System.currentTimeMillis();
        linesCovidComplete = covidCompleteService.importData();
        endTime = System.currentTimeMillis();
        durationCovidComplete = endTime - startTime;
        System.out.println("Import de covid_19_clean_complete terminé.");

        // 3. Import aggregated global data (full_grouped.csv)
        System.out.println("Import de full_grouped...");
        startTime = System.currentTimeMillis();
        linesFullGrouped = fullGroupedService.importData();
        endTime = System.currentTimeMillis();
        durationFullGrouped = endTime - startTime;
        System.out.println("Import de full_grouped terminé.");

        // 4. Import USA county data (usa_county_wise.csv)
        System.out.println("Import des données USA par états...");
        startTime = System.currentTimeMillis();
        linesUsaCounty = usaCountyService.importData();
        endTime = System.currentTimeMillis();
        durationUsaCounty = endTime - startTime;
        System.out.println("Import USA par états terminé.");

        // Calculate total import duration
        long totalImportTime = System.currentTimeMillis() - importStartTime;

        // Retrieve counts from the database
        long countryCount = countryRepository.count();
        long regionCount = regionRepository.count();
        long locationCount = locationRepository.count();
        long diseaseCount = diseaseRepository.count();
        long diseaseCaseCount = diseaseCaseRepository.count();

        System.out.println("************************************************************************************");
        System.out.println("***************************** BILAN DE L'IMPORTATION ETL ***************************");
        System.out.println("************************************************************************************");
        System.out.println();

        System.out.printf("%-25s : %10d ms%n%n", "Temps total d'importation", totalImportTime);

        System.out.println("worldometer_data.csv:");
        System.out.printf("    %-25s : %10d%n", "Lignes lues", linesWorldometer);
        System.out.printf("    %-25s : %10d ms%n%n", "Durée", durationWorldometer);

        System.out.println("covid_19_clean_complete.csv:");
        System.out.printf("    %-25s : %10d%n", "Lignes lues", linesCovidComplete);
        System.out.printf("    %-25s : %10d ms%n%n", "Durée", durationCovidComplete);

        System.out.println("full_grouped.csv:");
        System.out.printf("    %-25s : %10d%n", "Lignes lues", linesFullGrouped);
        System.out.printf("    %-25s : %10d ms%n%n", "Durée", durationFullGrouped);

        System.out.println("usa_county_wise.csv:");
        System.out.printf("    %-25s : %10d%n", "Lignes lues", linesUsaCounty);
        System.out.printf("    %-25s : %10d ms%n%n", "Durée", durationUsaCounty);

        System.out.printf("%-50s : %10d%n", "Nombre de maladies insérées (Disease)", diseaseCount);
        System.out.printf("%-50s : %10d%n", "Nombre de pays (Country) insérés", countryCount);
        System.out.printf("%-50s : %10d%n", "Nombre de régions (Region) insérées", regionCount);
        System.out.printf("%-50s : %10d%n", "Nombre de localisations (Location) insérées", locationCount);
        System.out.printf("%-50s : %10d%n", "Nombre de cas de maladie insérés (DiseaseCase)", diseaseCaseCount);

        System.out.println("************************************************************************************");



        System.out.println("** Importation des données terminée avec succès **");

    }

    public void deleteAllDataInBatch() {
        diseaseCaseRepository.deleteAllInBatch();
        diseaseRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
        regionRepository.deleteAllInBatch();
        countryRepository.deleteAllInBatch();
    }

}