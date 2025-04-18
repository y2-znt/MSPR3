package mspr.backend.etl.service;

import jakarta.transaction.Transactional;
import mspr.backend.BO.*;
import mspr.backend.etl.dto.FullGroupedDto;
import mspr.backend.etl.helpers.*;
import mspr.backend.etl.mapper.FullGroupedMapper;
import mspr.backend.etl.exceptions.*;
import mspr.backend.Repository.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FullGroupedService {

    private static final Logger logger = LoggerFactory.getLogger(FullGroupedService.class);
    public static final String FILE_NAME = "full_grouped.csv";
    
    // CSV field indices for full_grouped.csv
    private static final int IDX_DATE = 0;
    private static final int IDX_COUNTRY_REGION = 1;
    private static final int IDX_CONFIRMED = 2;
    private static final int IDX_DEATHS = 3;
    private static final int IDX_RECOVERED = 4;
    private static final int IDX_ACTIVE = 5;
    private static final int IDX_WHO_REGION = 9;
    private static final int MIN_FIELDS_REQUIRED = 10;

    @Autowired
    private FullGroupedMapper mapper;

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private DiseaseRepository diseaseRepository;

    @Autowired
    private CleanerHelper cleanerHelper;
    @Autowired
    private CacheHelper cacheHelper;

    /**
     * Import data from Full Grouped CSV file
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
            HashMap<Integer, FullGroupedDto> dtoMap = processCsvLines(lines);
            
            // Map DTOs to entities
            List<DiseaseCase> diseaseCases = mapDtosToEntities(dtoMap);
            
            // Save related entities and update cache
            saveRelatedEntities();
            
            // Update references in disease cases
            updateDiseaseCaseReferences(diseaseCases);
            
            // Save disease cases
            logger.info("Saving {} disease cases", diseaseCases.size());
            diseaseCaseRepository.saveAll(diseaseCases);
            
            logger.info("Full Grouped import completed: {} cases inserted", diseaseCases.size());
            
            return (lines.size()-1);
        } catch (DataFileNotFoundException | IOException | PersistenceException | MappingException e) {
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
     * Processes CSV lines into DTOs
     * 
     * @param lines Lines from the CSV file
     * @return Map of processed DTOs
     */
    private HashMap<Integer, FullGroupedDto> processCsvLines(List<String> lines) {
        HashMap<Integer, FullGroupedDto> dtoMap = new HashMap<>();
        int lineErrors = 0;
        
        // Skip header
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        logger.debug("Processing data lines...");
        
        // Process each line starting from line 1 (after header)
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            try {
                FullGroupedDto dto = processLine(lines.get(lineIndex), dateFormatter, lineIndex);
                if (dto != null) {
                    int hashKey = (dto.getDate() + dto.getCountryRegion() + dto.getConfirmed() + 
                                  dto.getDeaths() + dto.getRecovered() + dto.getActive()).hashCode();
                    dtoMap.put(hashKey, dto);
                } else {
                    lineErrors++;
                }
            } catch (Exception e) {
                logger.warn("Line {}: Unexpected error processing line: {}", lineIndex, e.getMessage());
                lineErrors++;
            }
        }

        logger.info("Processed {} unique records with {} errors", dtoMap.size(), lineErrors);
        return dtoMap;
    }
    
    /**
     * Processes a single CSV line into a DTO
     * 
     * @param line CSV line
     * @param dateFormatter Date formatter
     * @param lineIndex Line index for logging
     * @return DTO or null if the line couldn't be processed
     */
    private FullGroupedDto processLine(String line, DateTimeFormatter dateFormatter, int lineIndex) {
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (fields.length < MIN_FIELDS_REQUIRED) {
            logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.", 
                    lineIndex, MIN_FIELDS_REQUIRED, fields.length);
            return null;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(fields[IDX_DATE].trim(), dateFormatter);
        } catch (DateTimeParseException e) {
            logger.warn("Line {}: Error parsing date: {}", lineIndex, e.getMessage());
            return null;
        }
        
        String countryRegionName = cleanerHelper.cleanRegionName(
                cleanerHelper.cleanCountryName(fields[IDX_COUNTRY_REGION].trim()));
        
        int confirmed = 0, deaths = 0, recovered = 0, active = 0;
        try {
            confirmed = fields[IDX_CONFIRMED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_CONFIRMED].trim());
            deaths = fields[IDX_DEATHS].isEmpty() ? 0 : Integer.parseInt(fields[IDX_DEATHS].trim());
            recovered = fields[IDX_RECOVERED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_RECOVERED].trim());
            active = fields[IDX_ACTIVE].isEmpty() ? 0 : Integer.parseInt(fields[IDX_ACTIVE].trim());
        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing numeric fields: {}", lineIndex, e.getMessage());
            return null;
        }
        
        String whoRegion = fields[IDX_WHO_REGION].trim();

        return new FullGroupedDto(
                date,
                countryRegionName,
                confirmed,
                deaths,
                recovered,
                active,
                whoRegion
        );
    }
    
    /**
     * Maps DTOs to entities
     * 
     * @param dtoMap Map of DTOs
     * @return List of mapped entities
     * @throws MappingException if there is an error during mapping
     */
    private List<DiseaseCase> mapDtosToEntities(HashMap<Integer, FullGroupedDto> dtoMap) throws MappingException {
        List<DiseaseCase> diseaseCases = new ArrayList<>();
        logger.debug("Converting DTOs to entities...");
        
        for (FullGroupedDto dto : dtoMap.values()) {
            try {
                DiseaseCase diseaseCase = mapper.toEntity(dto);
                diseaseCases.add(diseaseCase);
            } catch (Exception e) {
                logger.warn("Error mapping DTO to entity: {}", e.getMessage());
                throw new MappingException("Error mapping FullGroupedDto to entity objects", e);
            }
        }
        
        return diseaseCases;
    }
    
    /**
     * Saves related entities (countries, regions, locations, diseases) and updates the cache
     * 
     * @throws PersistenceException if there's an error saving entities
     */
    private void saveRelatedEntities() throws PersistenceException {
        try {
            logger.debug("Saving entities to database...");
            
            // 1. Countries
            saveCountries();

            // 2. Regions
            saveRegions();

            // 3. Locations
            saveLocations();

            // 4. Diseases
            saveDiseases();
            
        } catch (DataAccessException e) {
            logger.error("Database error while saving entities: {}", e.getMessage());
            throw new PersistenceException("Error saving Full Grouped data to database", e);
        }
    }
    
    /**
     * Saves countries and updates the cache
     */
    private void saveCountries() {
        Map<String, Country> countriesToSave = cacheHelper.getCountries();
        logger.debug("Saving {} countries", countriesToSave.size());
        if (!countriesToSave.isEmpty()) {
            List<Country> savedCountries = countryRepository.saveAll(countriesToSave.values());
            cacheHelper.setCountries(savedCountries); // Update cache with managed entities
            logger.debug("Updated country cache with {} managed entities", savedCountries.size());
        } else {
            logger.debug("No new countries to save.");
        }
    }
    
    /**
     * Saves regions and updates the cache
     */
    private void saveRegions() {
        Map<String, Region> regionsToSave = cacheHelper.getRegions();
        logger.debug("Saving {} regions", regionsToSave.size());
        if (!regionsToSave.isEmpty()) {
            List<Region> savedRegions = regionRepository.saveAll(regionsToSave.values());
            cacheHelper.setRegions(savedRegions); // Update cache with managed entities
            logger.debug("Updated region cache with {} managed entities", savedRegions.size());
        } else {
            logger.debug("No new regions to save.");
        }
    }
    
    /**
     * Saves locations and updates the cache
     */
    private void saveLocations() {
        Map<String, Location> locationsToSave = cacheHelper.getLocations();
        logger.debug("Saving {} locations", locationsToSave.size());
        if (!locationsToSave.isEmpty()) {
            List<Location> savedLocations = locationRepository.saveAll(locationsToSave.values());
            cacheHelper.setLocations(savedLocations); // Update cache with managed entities
            logger.debug("Updated location cache with {} managed entities", savedLocations.size());
        } else {
            logger.debug("No new locations to save.");
        }
    }
    
    /**
     * Saves diseases and updates the cache
     */
    private void saveDiseases() {
        Map<String, Disease> diseasesToSave = cacheHelper.getDiseases();
        logger.debug("Saving {} diseases", diseasesToSave.size());
        if (!diseasesToSave.isEmpty()) {
            List<Disease> savedDiseases = diseaseRepository.saveAll(diseasesToSave.values());
            cacheHelper.setDiseases(savedDiseases); // Update cache with managed entities
            logger.debug("Updated disease cache with {} managed entities", savedDiseases.size());
        } else {
            logger.debug("No new diseases to save.");
        }
    }
    
    /**
     * Updates references in disease cases to managed entities
     * 
     * @param diseaseCases List of disease cases to update
     */
    private void updateDiseaseCaseReferences(List<DiseaseCase> diseaseCases) {
        // Set managed disease entities
        for (DiseaseCase dc : diseaseCases) {
            if (dc.getDisease() != null) {
                String diseaseName = dc.getDisease().getName();
                Disease managedDisease = cacheHelper.getDiseases().get(diseaseName);
                if (managedDisease != null) {
                    dc.setDisease(managedDisease);
                } else {
                    logger.warn("Could not find managed Disease entity in cache for name: {}", diseaseName);
                }
            }
        }
    }
}
