package mspr.backend.etl.service;

import jakarta.transaction.Transactional;
import mspr.backend.etl.dto.WorldometerDto;
import mspr.backend.etl.mapper.WorldometerMapper;
import mspr.backend.etl.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class WorldometerService extends AbstractCsvImportService<WorldometerDto> {

    private final Logger logger = LoggerFactory.getLogger(WorldometerService.class);
    private static final String FILE_NAME = "worldometer_data.csv";
    
    // CSV field indices for worldometer_data.csv
    private static final int IDX_COUNTRY_NAME = 0;
    private static final int IDX_CONTINENT = 1;
    private static final int IDX_POPULATION = 2;
    private static final int IDX_TOTAL_CASES = 3;
    private static final int IDX_TOTAL_DEATHS = 5;
    private static final int IDX_TOTAL_RECOVERED = 7;
    private static final int IDX_ACTIVE_CASES = 9;
    private static final int IDX_WHO_REGION = 15;
    private static final int MIN_FIELDS_REQUIRED = 16;

    @Autowired
    private WorldometerMapper mapper;

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected WorldometerDto processLine(String[] fields, int lineNumber) {
        if (fields.length < MIN_FIELDS_REQUIRED) {
            logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.",
                    lineNumber, MIN_FIELDS_REQUIRED, fields.length);
            return null;
        }

        // Use cleanerHelper for country name
        String countryName = cleanerHelper.cleanCountryName(fields[IDX_COUNTRY_NAME].trim());
        String continent = fields[IDX_CONTINENT].trim();
        long population = 0; // Use long for population
        int totalCases = 0, totalDeaths = 0, totalRecovered = 0, activeCases = 0;

        try {
            // Remove commas before parsing numbers
            String populationStr = fields[IDX_POPULATION].trim().replace(",", "");
            String totalCasesStr = fields[IDX_TOTAL_CASES].trim().replace(",", "");
            String totalDeathsStr = fields[IDX_TOTAL_DEATHS].trim().replace(",", "");
            String totalRecoveredStr = fields[IDX_TOTAL_RECOVERED].trim().replace(",", "");
            String activeCasesStr = fields[IDX_ACTIVE_CASES].trim().replace(",", "");

            population = populationStr.isEmpty() ? 0 : Long.parseLong(populationStr);
            totalCases = totalCasesStr.isEmpty() ? 0 : Integer.parseInt(totalCasesStr);
            totalDeaths = totalDeathsStr.isEmpty() ? 0 : Integer.parseInt(totalDeathsStr);
            totalRecovered = totalRecoveredStr.isEmpty() ? 0 : Integer.parseInt(totalRecoveredStr);
            activeCases = activeCasesStr.isEmpty() ? 0 : Integer.parseInt(activeCasesStr);

        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing numeric fields (commas removed): {}. Skipping line.", lineNumber, e.getMessage());
            return null; // Skip line if core numeric data is invalid
        }

        String whoRegion = fields[IDX_WHO_REGION].trim();
        // Use cleanerHelper for region name if appropriate, assuming WHO region might need it
        whoRegion = cleanerHelper.cleanRegionName(whoRegion);

        return new WorldometerDto(countryName, continent, population, totalCases, totalDeaths, totalRecovered, activeCases, whoRegion);
    }

    /**
     * Processes the DTO by passing it to the mapper.
     * The mapper is expected to interact with the CacheHelper to find/create/update
     * Country, Region, Location entities based on the DTO data.
     *
     * @param dto The DTO created from a CSV line.
     * @throws MappingException if mapping fails.
     */
    @Override
    protected void processDto(WorldometerDto dto) throws MappingException {
        try {
            // The mapper interacts with cacheHelper (injected in abstract class)
            // to get/create/update Country, Region, Location etc.
            mapper.toEntity(dto);
        } catch (Exception e) {
            logger.warn("Error processing WorldometerDto (likely during mapping/cache update): {}", e.getMessage(), e);
            // Decide if this should be a fatal MappingException or just a warning
            // If related entities are crucial, throw exception:
             throw new MappingException("Error processing WorldometerDto", e);
        }
    }

    /**
     * No specific post-processing is needed for WorldometerService.
     * The abstract class handles saving entities accumulated in the cache via PersistenceHelper
     * *before* this method is called.
     */
    @Override
    protected void postProcessing() {
        logger.info("Worldometer data processing finished. Related entities (Countries, Regions, Locations) have been persisted based on cache state.");
        // No entities list to save or clear here
    }
}
