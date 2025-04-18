package mspr.backend.etl.service;

import jakarta.transaction.Transactional;
import mspr.backend.BO.Country;
import mspr.backend.BO.Region;
import mspr.backend.BO.Location;
import mspr.backend.etl.dto.WorldometerDto;
import mspr.backend.etl.mapper.WorldometerMapper;
import mspr.backend.etl.exceptions.*;
import mspr.backend.Repository.CountryRepository;
import mspr.backend.Repository.RegionRepository;
import mspr.backend.Repository.LocationRepository;
import mspr.backend.etl.helpers.CacheHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class WorldometerService {

    private static final Logger logger = LoggerFactory.getLogger(WorldometerService.class);
    public static final String FILE_NAME = "worldometer_data.csv";
    
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

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CacheHelper cacheHelper;

    /**
     * Import data from Worldometer CSV file using shared CacheHelper and batch saving.
     *
     * @return Number of lines processed from the file
     * @throws DataFileNotFoundException If the required data file is not found
     * @throws IOException If there's an error reading the file
     * @throws PersistenceException If there's an error saving data to the database
     * @throws EtlException For other ETL-related errors
     */
    public int importData() throws DataFileNotFoundException, IOException, PersistenceException, EtlException {
        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        // Check if file exists first
        if (!Files.isRegularFile(path) || !Files.exists(path)) {
            logger.error("File {} does not exist or is not a regular file. Import aborted.", FILE_NAME);
            throw new DataFileNotFoundException(FILE_NAME);
        }
        logger.info("File {} found. Starting import process.", FILE_NAME);

        try {
            // Read CSV file
            List<String> lines = readCsvFile(path);
            logger.info("Read {} lines from {}", lines.size(), FILE_NAME);

            // Process DTOs
            HashMap<Integer, WorldometerDto> dtoMap = processAndMapDtos(lines);
            
            // Save related entities and update cache
            int[] entityCounts = saveEntities();
            
            logger.info("Worldometer import completed: {} countries, {} regions, and {} locations potentially updated/inserted.",
                    entityCounts[0], entityCounts[1], entityCounts[2]);

            return (lines.size() - 1); // Return raw lines processed count

        } catch (DataFileNotFoundException | IOException | PersistenceException e) {
            // Let these specific exceptions propagate
            throw e;
        } catch (Exception e) {
            // Wrap any other exceptions
            logger.error("Unexpected error during import of {}: {}", FILE_NAME, e.getMessage(), e);
            throw new EtlException("Unexpected error during import of " + FILE_NAME, e);
        }
    }
    
    /**
     * Reads CSV file and returns lines
     * 
     * @param path Path to the CSV file
     * @return List of lines from the CSV file
     * @throws IOException if there is an error reading the file
     * @throws DataFileNotFoundException if the file is not found
     */
    private List<String> readCsvFile(Path path) throws IOException, DataFileNotFoundException {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new DataFileNotFoundException(FILE_NAME, e);
        } catch (IOException e) {
            logger.error("IO error reading file {}: {}", FILE_NAME, e.getMessage());
            throw e; // rethrow as it's already a specific exception
        }
    }
    
    /**
     * Processes CSV lines into DTOs and maps them to entities
     * 
     * @param lines Lines from the CSV file
     * @return Map of processed DTOs
     */
    private HashMap<Integer, WorldometerDto> processAndMapDtos(List<String> lines) {
        HashMap<Integer, WorldometerDto> dtoMap = new HashMap<>(); // Use map to store unique DTOs based on hash
        int lineErrors = 0;
        int mappingErrors = 0;

        logger.debug("Processing data lines and mapping DTOs...");
        // Skip header
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            try {
                WorldometerDto dto = processLine(lines.get(lineIndex), lineIndex);
                if (dto != null) {
                    // Use hash to potentially avoid processing exact duplicate lines
                    int hashKey = (dto.getCountry() + dto.getContinent() + dto.getPopulation() + 
                            dto.getTotalCases() + dto.getTotalDeaths() + dto.getTotalRecovered() + dto.getActiveCases()).hashCode();
                    dtoMap.put(hashKey, dto);
                } else {
                    lineErrors++;
                }
            } catch (Exception e) {
                logger.warn("Line {}: Unexpected error processing line: {}", lineIndex, e.getMessage());
                lineErrors++;
            }
        }
        logger.info("Processed {} unique DTO records with {} line errors", dtoMap.size(), lineErrors);

        // Map DTOs using mapper which populates cacheHelper
        mapDtosToEntities(dtoMap.values(), mappingErrors);
        
        return dtoMap;
    }
    
    /**
     * Processes a single CSV line into a DTO
     * 
     * @param line CSV line
     * @param lineIndex Line index for logging
     * @return DTO or null if the line couldn't be processed
     */
    private WorldometerDto processLine(String line, int lineIndex) {
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (fields.length < MIN_FIELDS_REQUIRED) {
            logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.",
                    lineIndex, MIN_FIELDS_REQUIRED, fields.length);
            return null;
        }

        String countryName = fields[IDX_COUNTRY_NAME];
        String continent = fields[IDX_CONTINENT]; 
        int population = 0, totalCases = 0, totalDeaths = 0, totalRecovered = 0, activeCases = 0;

        try {
            // Use parseLong for population to handle larger numbers, fallback to 0
            population = fields[IDX_POPULATION].isEmpty() ? 0 : Integer.parseInt(fields[IDX_POPULATION].replace(",", ""));
            totalCases = fields[IDX_TOTAL_CASES].isEmpty() ? 0 : Integer.parseInt(fields[IDX_TOTAL_CASES].replace(",", ""));
            totalDeaths = fields[IDX_TOTAL_DEATHS].isEmpty() ? 0 : Integer.parseInt(fields[IDX_TOTAL_DEATHS].replace(",", ""));
            totalRecovered = fields[IDX_TOTAL_RECOVERED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_TOTAL_RECOVERED].replace(",", ""));
            activeCases = fields[IDX_ACTIVE_CASES].isEmpty() ? 0 : Integer.parseInt(fields[IDX_ACTIVE_CASES].replace(",", ""));
        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing numeric fields (commas removed): {}", lineIndex, e.getMessage());
            return null;
        }

        String whoRegion = fields[IDX_WHO_REGION];

        return new WorldometerDto(countryName, continent, population, totalCases, totalDeaths, totalRecovered, activeCases, whoRegion);
    }
    
    /**
     * Maps DTOs to entities using the mapper
     * 
     * @param dtos Collection of DTOs to map
     * @param mappingErrors Counter for mapping errors
     */
    private void mapDtosToEntities(Iterable<WorldometerDto> dtos, int mappingErrors) {
        logger.debug("Mapping DTOs to entities using shared cache...");
        for (WorldometerDto dto : dtos) {
            try {
                mapper.toEntity(dto); // Call mapper - it uses injected cacheHelper
            } catch (Exception e) {
                logger.warn("Error mapping DTO to entity: {}", e.getMessage());
                mappingErrors++; // Count mapping errors
            }
        }
        logger.info("Finished mapping DTOs with {} mapping errors", mappingErrors);
    }
    
    /**
     * Saves entities (countries, regions, locations) to the database and updates the cache
     * 
     * @return Array with counts of saved entities: [countries, regions, locations]
     * @throws PersistenceException if there's an error saving entities
     */
    private int[] saveEntities() throws PersistenceException {
        int savedCountriesCount = 0;
        int savedRegionsCount = 0;
        int savedLocationsCount = 0;
        
        try {
            logger.debug("Saving cached related entities to database...");

            // 1. Countries
            savedCountriesCount = saveCountries();

            // 2. Regions
            savedRegionsCount = saveRegions();

            // 3. Locations
            savedLocationsCount = saveLocations();

        } catch (DataAccessException e) {
            logger.error("Database error while saving entities: {}", e.getMessage());
            throw new PersistenceException("Error saving Worldometer data to database", e);
        }
        
        return new int[] { savedCountriesCount, savedRegionsCount, savedLocationsCount };
    }
    
    /**
     * Saves countries and updates the cache
     * 
     * @return Number of countries saved
     */
    private int saveCountries() {
        Map<String, Country> countriesToSave = cacheHelper.getCountries();
        logger.debug("Saving {} countries", countriesToSave.size());
        if (!countriesToSave.isEmpty()) {
            List<Country> savedCountries = countryRepository.saveAll(countriesToSave.values());
            int savedCount = savedCountries.size();
            cacheHelper.setCountries(savedCountries); // Update cache with managed entities
            logger.debug("Updated country cache with {} managed entities", savedCount);
            return savedCount;
        } else {
            logger.debug("No new countries to save.");
            return 0;
        }
    }
    
    /**
     * Saves regions and updates the cache
     * 
     * @return Number of regions saved
     */
    private int saveRegions() {
        Map<String, Region> regionsToSave = cacheHelper.getRegions();
        logger.debug("Saving {} regions", regionsToSave.size());
        if (!regionsToSave.isEmpty()) {
            List<Region> savedRegions = regionRepository.saveAll(regionsToSave.values());
            int savedCount = savedRegions.size();
            cacheHelper.setRegions(savedRegions); // Update cache with managed entities
            logger.debug("Updated region cache with {} managed entities", savedCount);
            return savedCount;
        } else {
            logger.debug("No new regions to save.");
            return 0;
        }
    }
    
    /**
     * Saves locations and updates the cache
     * 
     * @return Number of locations saved
     */
    private int saveLocations() {
        Map<String, Location> locationsToSave = cacheHelper.getLocations();
        logger.debug("Saving {} locations", locationsToSave.size());
        if (!locationsToSave.isEmpty()) {
            List<Location> savedLocations = locationRepository.saveAll(locationsToSave.values());
            int savedCount = savedLocations.size();
            cacheHelper.setLocations(savedLocations); // Update cache with managed entities
            logger.debug("Updated location cache with {} managed entities", savedCount);
            return savedCount;
        } else {
            logger.debug("No new locations to save.");
            return 0;
        }
    }
}
