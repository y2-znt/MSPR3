package mspr.backend.etl.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mspr.backend.BO.*;
import mspr.backend.etl.dto.UsaCountyDto;
import mspr.backend.etl.helpers.*;
import mspr.backend.etl.mapper.UsaCountyMapper;
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
import java.util.List;
import java.util.Map;
import jakarta.transaction.Transactional;

@Service
public class UsaCountyService {

    private static final Logger logger = LoggerFactory.getLogger(UsaCountyService.class);

    // CSV field indices for usa_county_wise.csv
    private static final int IDX_COUNTY = 5; // Admin2
    private static final int IDX_PROVINCE_STATE = 6;
    private static final int IDX_COUNTRY_REGION = 7;
    private static final int IDX_LATITUDE = 8;
    private static final int IDX_LONGITUDE = 9;
    private static final int IDX_DATE = 11;
    private static final int IDX_CONFIRMED = 12;
    private static final int IDX_DEATHS = 13;
    private static final int MIN_FIELDS_REQUIRED = 14;

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
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UsaCountyMapper usaCountyMapper;
    @Autowired
    private CacheHelper cacheHelper;
    @Autowired
    private CleanerHelper cleanerHelper;

    public static final String FILE_NAME = "usa_county_wise.csv";
    public static final String COVID_19_DISEASE_NAME = "COVID-19";

    /**
     * Import data from USA county CSV file using shared CacheHelper and batch saving.
     *
     * @return Number of lines processed from the file
     * @throws DataFileNotFoundException If the required data file is not found
     * @throws IOException If there's an error reading the file
     * @throws PersistenceException If there's an error saving data to the database
     * @throws EtlException For other ETL-related errors
     */
    @Transactional
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
            // Ensure COVID-19 disease exists and is in cache
            ensureCovidDiseaseExists();

            // Read CSV file
            List<String> lines = readCsvFile(path);
            logger.info("Read {} lines from {}", lines.size(), FILE_NAME);

            // Process DTOs
            List<UsaCountyDto> dtos = processCsvLines(lines);
            logger.info("Processed {} DTO records", dtos.size());

            // Map DTOs to entities
            List<DiseaseCase> diseaseCases = mapDtosToEntities(dtos);
            logger.info("Mapped {} DTOs to entities", diseaseCases.size());

            // Save related entities and update cache
            saveRelatedEntities();

            // Update DiseaseCase references to managed entities
            updateDiseaseCaseReferences(diseaseCases);

            // Save disease cases
            logger.info("Saving {} disease cases to database", diseaseCases.size());
            diseaseCaseRepository.saveAll(diseaseCases);

            logger.info("USA county data import completed: {} cases inserted", diseaseCases.size());

            return (lines.size()-1); // Return raw lines processed count

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
     * Ensures the COVID-19 disease exists in the database and cache
     * 
     * @throws PersistenceException if there is an error persisting the disease entity
     */
    private void ensureCovidDiseaseExists() throws PersistenceException {
        Disease covidDisease = cacheHelper.getDiseases().get(COVID_19_DISEASE_NAME);
        if (covidDisease == null) {
            covidDisease = diseaseRepository.findByName(COVID_19_DISEASE_NAME);
            if (covidDisease == null) {
                logger.debug("Creating COVID-19 disease entity");
                covidDisease = new Disease();
                covidDisease.setName(COVID_19_DISEASE_NAME);
                try {
                    covidDisease = diseaseRepository.save(covidDisease);
                } catch (DataAccessException e) {
                    logger.error("Failed to save COVID-19 disease: {}", e.getMessage());
                    throw new PersistenceException("Failed to save essential COVID-19 disease entity", e);
                }
            }
            cacheHelper.addDiseaseToCache(COVID_19_DISEASE_NAME, covidDisease);
            logger.debug("Ensured COVID-19 disease is in cache with ID: {}", covidDisease.getId());
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
            throw e;
        }
    }
    
    /**
     * Processes CSV lines into DTOs
     * 
     * @param lines Lines from the CSV file
     * @return List of DTOs
     */
    private List<UsaCountyDto> processCsvLines(List<String> lines) {
        List<UsaCountyDto> dtos = new ArrayList<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("M/d/yy");
        int lineErrors = 0;

        logger.debug("Processing data lines into DTOs...");
        // Skip header
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            try {
                UsaCountyDto dto = processLine(lines.get(lineIndex), dateFmt, lineIndex);
                if (dto != null) {
                    dtos.add(dto);
                } else {
                    lineErrors++;
                }
            } catch (Exception e) {
                logger.warn("Line {}: Unexpected error processing line: {}", lineIndex, e.getMessage());
                lineErrors++;
            }
        }
        logger.info("Processed {} DTO records with {} line errors", dtos.size(), lineErrors);
        return dtos;
    }
    
    /**
     * Processes a single CSV line into a DTO
     * 
     * @param line CSV line
     * @param dateFmt Date formatter
     * @param lineIndex Line index for logging
     * @return DTO or null if the line couldn't be processed
     */
    private UsaCountyDto processLine(String line, DateTimeFormatter dateFmt, int lineIndex) {
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (fields.length < MIN_FIELDS_REQUIRED) {
            logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.",
                    lineIndex, MIN_FIELDS_REQUIRED, fields.length);
            return null;
        }

        // Extract fields
        String county = fields[IDX_COUNTY].trim();
        String provinceState = fields[IDX_PROVINCE_STATE].trim();
        String countryRegion = fields[IDX_COUNTRY_REGION].trim();
        double lat = 0.0, lon = 0.0;
        LocalDate date;
        int confirmed = 0, deaths = 0;

        try {
            lat = Double.parseDouble(fields[IDX_LATITUDE].trim());
            lon = Double.parseDouble(fields[IDX_LONGITUDE].trim());
        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing geographic coordinates: {}. Setting to 0.", lineIndex, e.getMessage());
            // No need to skip, just use 0
        }

        try {
            date = LocalDate.parse(fields[IDX_DATE].trim(), dateFmt);
        } catch (DateTimeParseException e) {
            logger.warn("Line {}: Error parsing date: {}. Skipping line.", lineIndex, e.getMessage());
            return null;
        }

        try {
            confirmed = fields[IDX_CONFIRMED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_CONFIRMED].trim());
            deaths = fields[IDX_DEATHS].isEmpty() ? 0 : Integer.parseInt(fields[IDX_DEATHS].trim());
        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing numeric fields: {}. Setting to 0.", lineIndex, e.getMessage());
            // No need to skip, just use 0
        }

        // "recovered" and "active" data are not present in this file
        int recovered = 0;
        int active = 0;

        // Clean names
        countryRegion = cleanerHelper.cleanCountryName(countryRegion);
        provinceState = cleanerHelper.cleanRegionName(provinceState);

        return new UsaCountyDto(county, provinceState, countryRegion, lat, lon, date, confirmed, deaths, recovered, active);
    }
    
    /**
     * Maps DTOs to entities
     * 
     * @param dtos List of DTOs
     * @return List of mapped entities
     */
    private List<DiseaseCase> mapDtosToEntities(List<UsaCountyDto> dtos) {
        List<DiseaseCase> diseaseCases = new ArrayList<>();
        int mappingErrors = 0;
        logger.debug("Converting DTOs to entities using shared cache...");
        
        for (UsaCountyDto dto : dtos) {
            try {
                // Mapper uses the injected cacheHelper to get/create related entities and populates cache
                DiseaseCase diseaseCase = usaCountyMapper.fromDto(dto, cacheHelper);
                diseaseCases.add(diseaseCase);
            } catch (IllegalStateException e) { // Catch specific error from mapper if disease missing
                logger.error("Mapping error: {}", e.getMessage());
                mappingErrors++;
            } catch (Exception e) {
                logger.warn("Error mapping DTO to entity: {}", e.getMessage());
                mappingErrors++;
            }
        }
        logger.info("Mapped {} DTOs to entities with {} mapping errors", diseaseCases.size(), mappingErrors);
        return diseaseCases;
    }
    
    /**
     * Saves related entities (countries, regions, locations) and updates the cache
     * 
     * @throws PersistenceException if there's an error saving entities
     */
    private void saveRelatedEntities() throws PersistenceException {
        try {
            logger.debug("Saving cached related entities to database...");
            saveCountries();
            saveRegions();
            saveLocations();
        } catch (DataAccessException e) {
            logger.error("Database error during entity saving: {}", e.getMessage());
            throw new PersistenceException("Error saving USA County data to database", e);
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
     * Updates DiseaseCase references to managed entities
     * 
     * @param diseaseCases List of disease cases to update
     * @throws EtlException if there's an unexpected error during update
     */
    private void updateDiseaseCaseReferences(List<DiseaseCase> diseaseCases) throws EtlException {
        logger.debug("Updating DiseaseCase references to managed entities...");
        int updateErrors = 0;
        
        for (DiseaseCase dc : diseaseCases) {
            try {
                updateLocationReference(dc);
                updateDiseaseReference(dc);
            } catch (Exception e) {
                logger.error("Error updating DiseaseCase reference: {}", e.getMessage());
                updateErrors++;
            }
        }
        
        if (updateErrors > 0) {
            logger.warn("Encountered {} errors while updating DiseaseCase references.", updateErrors);
        }
    }
    
    /**
     * Updates the location reference for a disease case
     * 
     * @param dc Disease case to update
     */
    private void updateLocationReference(DiseaseCase dc) {
        Location originalLocation = dc.getLocation();
        if (originalLocation != null) {
            Region originalRegion = originalLocation.getRegion();
            if (originalRegion != null) {
                Country originalCountry = originalRegion.getCountry();
                if (originalCountry != null && originalRegion.getName() != null && originalLocation.getName() != null) {
                    String locationKey = originalCountry.getName() + "|" + originalRegion.getName() + "|" + originalLocation.getName();
                    Location managedLocation = cacheHelper.getLocations().get(locationKey);
                    if (managedLocation != null) {
                        dc.setLocation(managedLocation); // Set managed Location
                    } else {
                        logger.warn("Could not find managed Location in cache for key: {}", locationKey);
                    }
                } else {
                    logger.warn("Missing components to build location key for DiseaseCase update.");
                }
            } else {
                logger.warn("DiseaseCase has Location with null Region.");
            }
        } else {
            logger.warn("DiseaseCase has null Location.");
        }
    }
    
    /**
     * Updates the disease reference for a disease case
     * 
     * @param dc Disease case to update
     * @throws EtlException if COVID-19 disease is missing from cache
     */
    private void updateDiseaseReference(DiseaseCase dc) throws EtlException {
        Disease managedDisease = cacheHelper.getDiseases().get(COVID_19_DISEASE_NAME);
        if(managedDisease != null) {
            dc.setDisease(managedDisease);
        } else {
            // This should not happen if initial check succeeded
            logger.error("Critical Error: Managed COVID-19 disease missing from cache during final update.");
            throw new EtlException("COVID-19 disease missing unexpectedly");
        }
    }
}


