package mspr.backend.ETL.Runner;

import mspr.backend.BO.Disease;
import mspr.backend.ETL.Helpers.CacheHelper;
import mspr.backend.ETL.exceptions.*;
import mspr.backend.Repository.*;
import mspr.backend.ETL.Service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DataImportCovid19Runner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataImportCovid19Runner.class);

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

        logger.info("Starting COVID-19 data import process");
        logger.info("Deleting existing data...");
        deleteAllDataInBatch();
        logger.info("Existing data deletion completed");

        // Prepare COVID-19 Disease entity in the database and add it to the cache
        Disease covid = diseaseRepository.findByName("COVID-19");
        if (covid == null) {
            logger.debug("COVID-19 disease not found in database, creating new entity");
            covid = new Disease();
            covid.setName("COVID-19");
            covid = diseaseRepository.save(covid);
            logger.debug("COVID-19 disease entity created with ID: {}", covid.getId());
        } else {
            logger.debug("Found existing COVID-19 disease entity with ID: {}", covid.getId());
        }
        cacheHelper.addDiseaseToCache("COVID-19", covid);
        logger.debug("Added COVID-19 disease to cache");

        // Variables to hold durations and line counts for each import
        long startTime, endTime;
        long durationWorldometer = 0, durationCovidComplete = 0, durationFullGrouped = 0, durationUsaCounty = 0;
        int linesWorldometer = 0, linesCovidComplete = 0, linesFullGrouped = 0, linesUsaCounty = 0;

        logger.info("*** STARTING COVID DATA IMPORT ***");

        // 1. Import Worldometer Data
        logger.info("Importing Worldometer data...");
        startTime = System.currentTimeMillis();
        try {
            linesWorldometer = worldometerService.importData();
            endTime = System.currentTimeMillis();
            durationWorldometer = endTime - startTime;
            logger.info("Worldometer import completed: {} lines in {} ms", linesWorldometer, durationWorldometer);
        } catch (DataFileNotFoundException e) {
            logger.error("Worldometer data file not found: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationWorldometer = endTime - startTime;
        } catch (PersistenceException e) {
            logger.error("Database error during Worldometer import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationWorldometer = endTime - startTime;
        } catch (IOException e) {
            logger.error("IO error during Worldometer import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationWorldometer = endTime - startTime;
        } catch (EtlException e) {
            logger.error("Error during Worldometer import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationWorldometer = endTime - startTime;
        }

        // 2. Import detailed global data (covid_19_clean_complete.csv)
        logger.info("Importing covid_19_clean_complete data...");
        startTime = System.currentTimeMillis();
        try {
            linesCovidComplete = covidCompleteService.importData();
            endTime = System.currentTimeMillis();
            durationCovidComplete = endTime - startTime;
            logger.info("covid_19_clean_complete import completed: {} lines in {} ms", linesCovidComplete, durationCovidComplete);
        } catch (DataFileNotFoundException e) {
            logger.error("COVID complete data file not found: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationCovidComplete = endTime - startTime;
        } catch (PersistenceException e) {
            logger.error("Database error during COVID complete import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationCovidComplete = endTime - startTime;
        } catch (IOException e) {
            logger.error("IO error during COVID complete import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationCovidComplete = endTime - startTime;
        } catch (EtlException e) {
            logger.error("Error during COVID complete import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationCovidComplete = endTime - startTime;
        }

        // 3. Import aggregated global data (full_grouped.csv)
        logger.info("Importing full_grouped data...");
        startTime = System.currentTimeMillis();
        try {
            linesFullGrouped = fullGroupedService.importData();
            endTime = System.currentTimeMillis();
            durationFullGrouped = endTime - startTime;
            logger.info("full_grouped import completed: {} lines in {} ms", linesFullGrouped, durationFullGrouped);
        } catch (DataFileNotFoundException e) {
            logger.error("Full grouped data file not found: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationFullGrouped = endTime - startTime;
        } catch (PersistenceException e) {
            logger.error("Database error during Full grouped import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationFullGrouped = endTime - startTime;
        } catch (IOException e) {
            logger.error("IO error during Full grouped import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationFullGrouped = endTime - startTime;
        } catch (EtlException e) {
            logger.error("Error during Full grouped import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationFullGrouped = endTime - startTime;
        }

        // 4. Import USA county data (usa_county_wise.csv)
        logger.info("Importing USA county data...");
        startTime = System.currentTimeMillis();
        try {
            linesUsaCounty = usaCountyService.importData();
            endTime = System.currentTimeMillis();
            durationUsaCounty = endTime - startTime;
            logger.info("USA county data import completed: {} lines in {} ms", linesUsaCounty, durationUsaCounty);
        } catch (DataFileNotFoundException e) {
            logger.error("USA county data file not found: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationUsaCounty = endTime - startTime;
        } catch (PersistenceException e) {
            logger.error("Database error during USA county import: {}", e.getMessage());
                endTime = System.currentTimeMillis();
                durationUsaCounty = endTime - startTime;
        } catch (IOException e) {
            logger.error("IO error during USA county import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationUsaCounty = endTime - startTime;
        } catch (EtlException e) {
            logger.error("Error during USA county import: {}", e.getMessage());
            endTime = System.currentTimeMillis();
            durationUsaCounty = endTime - startTime;
        }

        // Calculate total import duration
        long totalImportTime = System.currentTimeMillis() - importStartTime;

        // Retrieve counts from the database
        long countryCount = countryRepository.count();
        long regionCount = regionRepository.count();
        long locationCount = locationRepository.count();
        long diseaseCount = diseaseRepository.count();
        long diseaseCaseCount = diseaseCaseRepository.count();

        // Create a tabular format summary without actual table borders
        logger.info("");
        logger.info("=========================== COVID-19 DATA IMPORT SUMMARY ===========================");
        logger.info("");
        logger.info("TOTAL IMPORT TIME: {} ms", totalImportTime);
        logger.info("");
        
        // File import statistics in tabular format
        String fileHeader = String.format("%-30s %12s %12s", "FILE", "LINES", "TIME (ms)");
        String fileSeparator = String.format("%-30s %12s %12s", "-".repeat(30), "-".repeat(12), "-".repeat(12));
        
        logger.info(fileHeader);
        logger.info(fileSeparator);
        logger.info(String.format("%-30s %,12d %,12d", "worldometer_data.csv", linesWorldometer, durationWorldometer));
        logger.info(String.format("%-30s %,12d %,12d", "covid_19_clean_complete.csv", linesCovidComplete, durationCovidComplete));
        logger.info(String.format("%-30s %,12d %,12d", "full_grouped.csv", linesFullGrouped, durationFullGrouped));
        logger.info(String.format("%-30s %,12d %,12d", "usa_county_wise.csv", linesUsaCounty, durationUsaCounty));
        logger.info("");
        
        // Database statistics in tabular format
        String dbHeader = String.format("%-30s %12s", "ENTITY TYPE", "COUNT");
        String dbSeparator = String.format("%-30s %12s", "-".repeat(30), "-".repeat(12));
        
        logger.info(dbHeader);
        logger.info(dbSeparator);
        logger.info(String.format("%-30s %,12d", "Diseases", diseaseCount));
        logger.info(String.format("%-30s %,12d", "Countries", countryCount));
        logger.info(String.format("%-30s %,12d", "Regions", regionCount));
        logger.info(String.format("%-30s %,12d", "Locations", locationCount));
        logger.info(String.format("%-30s %,12d", "Disease Cases", diseaseCaseCount));
        logger.info("");
        
        logger.info("=========================== IMPORT COMPLETED SUCCESSFULLY ===========================");
    }

    public void deleteAllDataInBatch() {
        logger.debug("Deleting all disease cases");
        diseaseCaseRepository.deleteAllInBatch();
        logger.debug("Deleting all diseases");
        diseaseRepository.deleteAllInBatch();
        logger.debug("Deleting all locations");
        locationRepository.deleteAllInBatch();
        logger.debug("Deleting all regions");
        regionRepository.deleteAllInBatch();
        logger.debug("Deleting all countries");
        countryRepository.deleteAllInBatch();
        logger.debug("All data deletion completed");
    }
}