package mspr.backend.etl.service;

import mspr.backend.etl.exceptions.*;
import mspr.backend.etl.helpers.cache.CacheManager;
import mspr.backend.etl.helpers.CleanerHelper;
import mspr.backend.etl.helpers.CsvReaderHelper;
import mspr.backend.etl.helpers.PersistenceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Abstract base class for services importing data from CSV files.
 * Provides a template method `importData` and common functionalities.
 *
 * @param <DTO> The type of Data Transfer Object specific to the CSV structure.
 */
public abstract class AbstractCsvImportService<DTO> {

    private final Logger logger = LoggerFactory.getLogger(getClass()); // Logger specific to the concrete class

    protected static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final String DATA_PATH_PREFIX = "src/main/resources/data/";

    @Autowired
    protected CsvReaderHelper csvReaderHelper;

    @Autowired
    protected PersistenceHelper persistenceHelper;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected CleanerHelper cleanerHelper; // Common helper, might not be used by all subclasses

    /**
     * Template method to import data from a CSV file.
     *
     * @return Number of lines processed (excluding header).
     * @throws DataFileNotFoundException If the required data file is not found.
     * @throws IOException If there's an error reading the file.
     * @throws PersistenceException If there's an error saving data to the database.
     * @throws EtlException For other ETL-related errors.
     */
    public int importData() throws DataFileNotFoundException, IOException, PersistenceException, EtlException {
        String fileName = getFileName();
        Path path = Paths.get(DATA_PATH_PREFIX + fileName);
        logger.info("Starting import process for {}", fileName);

        try {
            // 1. Pre-processing steps (e.g., ensure specific entities exist)
            preProcessing();

            // 2. Read CSV file
            List<String> lines = csvReaderHelper.readCsvFile(path, fileName);
            int totalLines = lines.size();
            if (totalLines <= 1) { // Only header or empty
                logger.warn("File {} is empty or contains only a header. No data to import.", fileName);
                return 0;
            }

            // 3. Process lines
            int processedLines = 0;
            int lineErrors = 0;
            logger.debug("Processing data lines...");
            for (int lineIndex = 1; lineIndex < totalLines; lineIndex++) { // Skip header (line 0)
                String currentLine = lines.get(lineIndex);
                try {
                    String[] fields = currentLine.split(CSV_SPLIT_REGEX, -1);
                    DTO dto = processLine(fields, lineIndex + 1); // lineIndex + 1 for 1-based logging
                    if (dto != null) {
                        processDto(dto);
                        processedLines++;
                    } else {
                        lineErrors++; // Error handled and logged within processLine or validation failed
                    }
                } catch (Exception e) {
                    logger.warn("Line {}: Unexpected error processing line: {}", lineIndex + 1, e.getMessage(), e);
                    lineErrors++;
                }
            }
            logger.info("Finished processing lines. Processed records: {}, Line errors: {}", processedLines, lineErrors);

            // 4. Persist cached related entities (Countries, Regions, etc.)
            persistenceHelper.persistCachedEntities();

            // 5. Post-processing steps (e.g., save main entities, update references)
            postProcessing();

            logger.info("Import process for {} completed successfully.", fileName);
            return processedLines; // Return number of successfully processed records

        } catch (DataFileNotFoundException | IOException | PersistenceException | MappingException e) {
            logger.error("ETL process failed for {}: {}", fileName, e.getMessage(), e);
            throw e; // Propagate specific, handled exceptions
        } catch (Exception e) {
            logger.error("Unexpected error during import of {}: {}", fileName, e.getMessage(), e);
            throw new EtlException("Unexpected error during import of " + fileName, e);
        }
    }

    // --- Abstract methods to be implemented by subclasses --- 

    /**
     * @return The specific name of the CSV file to be processed.
     */
    protected abstract String getFileName();

    /**
     * Performs any necessary setup before reading the CSV file.
     * For example, ensuring a specific entity (like a default Disease) exists.
     * Default implementation does nothing.
     */
    protected void preProcessing() throws Exception {
        // Default: no pre-processing needed
        logger.debug("No pre-processing steps required for {}", getClass().getSimpleName());
    }

    /**
     * Processes a single line (represented as an array of fields) from the CSV file.
     * Responsible for parsing fields, validating data, and creating a DTO.
     *
     * @param fields The fields extracted from the current CSV line.
     * @param lineNumber The 1-based line number in the file (for logging).
     * @return A DTO object representing the line, or null if the line is invalid or should be skipped.
     * @throws Exception If an unrecoverable error occurs during processing.
     */
    protected abstract DTO processLine(String[] fields, int lineNumber) throws Exception;

    /**
     * Processes a successfully created DTO.
     * This typically involves mapping the DTO to an entity and adding it to a collection for later batch saving,
     * or directly interacting with repositories/cache for simpler cases.
     *
     * @param dto The DTO created from a CSV line.
     * @throws Exception If an error occurs during DTO processing (e.g., mapping).
     */
    protected abstract void processDto(DTO dto) throws Exception;

    /**
     * Performs final steps after all lines are processed and related entities are saved.
     * This is typically used for saving the main entities collected during `processDto`
     * and updating any necessary references between entities.
     * Default implementation does nothing.
     */
    protected void postProcessing() throws Exception {
        // Default: no post-processing needed
        logger.debug("No post-processing steps required for {}", getClass().getSimpleName());
    }
} 