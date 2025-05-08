package mspr.backend.etl.runner;

import java.io.IOException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mspr.backend.entity.Disease;
import mspr.backend.etl.exceptions.DataFileNotFoundException;
import mspr.backend.etl.exceptions.EtlException;
import mspr.backend.etl.exceptions.PersistenceException;
import mspr.backend.etl.helpers.cache.CacheManager;
import mspr.backend.etl.service.CovidCompleteService;
import mspr.backend.etl.service.FullGroupedService;
import mspr.backend.etl.service.UsaCountyService;
import mspr.backend.etl.service.WorldometerService;
import mspr.backend.repository.CountryRepository;
import mspr.backend.repository.DiseaseCaseRepository;
import mspr.backend.repository.DiseaseRepository;
import mspr.backend.repository.LocationRepository;
import mspr.backend.repository.RegionRepository;

@Profile("!test")
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

    @Autowired private CacheManager cacheManager;

    // Define a simple class or record to hold the result of an import
    private static class ImportResult {
        final int linesProcessed;
        final long durationMs;

        ImportResult(int linesProcessed, long durationMs) {
            this.linesProcessed = linesProcessed;
            this.durationMs = durationMs;
        }
    }

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
        cacheManager.addDiseaseToCache("COVID-19", covid);
        logger.debug("Added COVID-19 disease to cache");

        // Use the helper method for each import
        logger.info("*** STARTING COVID DATA IMPORT ***");

        ImportResult worldometerResult = executeImport(() -> {
            try {
                return worldometerService.importData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "Worldometer");

        ImportResult covidCompleteResult = executeImport(() -> {
            try {
                return covidCompleteService.importData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "covid_19_clean_complete");

        ImportResult fullGroupedResult = executeImport(() -> {
            try {
                return fullGroupedService.importData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "full_grouped");

        ImportResult usaCountyResult = executeImport(() -> {
            try {
                return usaCountyService.importData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "USA county");

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
        logger.info(String.format("%-30s %,12d %,12d", "worldometer_data.csv", worldometerResult.linesProcessed, worldometerResult.durationMs));
        logger.info(String.format("%-30s %,12d %,12d", "covid_19_clean_complete.csv", covidCompleteResult.linesProcessed, covidCompleteResult.durationMs));
        logger.info(String.format("%-30s %,12d %,12d", "full_grouped.csv", fullGroupedResult.linesProcessed, fullGroupedResult.durationMs));
        logger.info(String.format("%-30s %,12d %,12d", "usa_county_wise.csv", usaCountyResult.linesProcessed, usaCountyResult.durationMs));
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

    /**
     * Executes an import task, handling timing and common exceptions.
     *
     * @param importTask A Supplier providing the import logic that returns the number of lines processed.
     *                   The supplier should wrap checked exceptions into RuntimeException.
     * @param taskName   The name of the import task for logging purposes.
     * @return An ImportResult containing the number of lines processed and the duration.
     */
    private ImportResult executeImport(Supplier<Integer> importTask, String taskName) {
        logger.info("Importing {} data...", taskName);
        long startTime = System.currentTimeMillis();
        int linesProcessed = 0;
        long durationMs = 0;

        try {
            linesProcessed = importTask.get();
            durationMs = System.currentTimeMillis() - startTime;
            logger.info("{} import completed: {} lines in {} ms", taskName, linesProcessed, durationMs);
        } catch (RuntimeException e) {
            // Unwrap the original exception if possible
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            durationMs = System.currentTimeMillis() - startTime; // Record duration even on failure

            if (cause instanceof DataFileNotFoundException) {
                logger.error("{} data file not found: {}", taskName, cause.getMessage());
            } else if (cause instanceof PersistenceException) {
                logger.error("Database error during {} import: {}", taskName, cause.getMessage());
            } else if (cause instanceof IOException) {
                logger.error("IO error during {} import: {}", taskName, cause.getMessage());
            } else if (cause instanceof EtlException) {
                logger.error("ETL error during {} import: {}", taskName, cause.getMessage());
            } else {
                logger.error("Unexpected error during {} import: {}", taskName, cause.getMessage(), cause); // Log stack trace for unexpected errors
            }
        } catch (Exception e) { // Catch any other unexpected exceptions
             durationMs = System.currentTimeMillis() - startTime;
             logger.error("Unexpected error during {} import: {}", taskName, e.getMessage(), e);
        }
        return new ImportResult(linesProcessed, durationMs);
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