package mspr.backend.ETL.Service;
import mspr.backend.BO.*;
import mspr.backend.ETL.DTO.CovidCompleteDto;
import mspr.backend.ETL.Helpers.*;
import mspr.backend.ETL.Mapper.CovidCompleteMapper;
import mspr.backend.ETL.exceptions.*;
import mspr.backend.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

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
import java.util.stream.Collectors;

@Service
public class CovidCompleteService {

    private static final Logger logger = LoggerFactory.getLogger(CovidCompleteService.class);
    public static final String FILE_NAME = "covid_19_clean_complete.csv";
    
    // CSV field indices for covid_19_clean_complete.csv
    private static final int IDX_PROVINCE_STATE = 0;
    private static final int IDX_COUNTRY_REGION = 1;
    private static final int IDX_LATITUDE = 2;
    private static final int IDX_LONGITUDE = 3;
    private static final int IDX_DATE = 4;
    private static final int IDX_CONFIRMED = 5;
    private static final int IDX_DEATHS = 6;
    private static final int IDX_RECOVERED = 7;
    private static final int IDX_ACTIVE = 8;
    private static final int IDX_WHO_REGION = 9;
    private static final int MIN_FIELDS_REQUIRED = 10;

    @Autowired
    private CovidCompleteMapper mapper;

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
    private CacheHelper cacheHelper;

    @Autowired
    private CleanerHelper cleanerHelper;

    /**
     * Import data from COVID-19 complete CSV file
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
            HashMap<Integer, CovidCompleteDto> dtoMap = processCsvLines(lines);
            logger.info("Processed {} unique records", dtoMap.size());

            // Map DTOs to entities
            List<DiseaseCase> diseaseCases = mapDtosToEntities(dtoMap);
            
            // Save related entities and update cache
            saveRelatedEntities();
            
            // Update references in disease cases
            updateDiseaseCaseReferences(diseaseCases);
            
            // Save disease cases
            logger.info("Saving {} disease cases", diseaseCases.size());
            diseaseCaseRepository.saveAll(diseaseCases);
            
            logger.info("COVID Complete import completed: {} cases inserted", diseaseCases.size());
            
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
    private HashMap<Integer, CovidCompleteDto> processCsvLines(List<String> lines) {
        HashMap<Integer, CovidCompleteDto> dtoMap = new HashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int lineErrors = 0;

        // Skip header
        logger.debug("Processing data lines...");
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            try {
                CovidCompleteDto dto = processLine(lines.get(lineIndex), dateFormatter, lineIndex);
                if (dto != null) {
                    int hashKey = (dto.getProvinceState() + dto.getCountryRegion() + dto.getLat() + dto.getLon() + 
                                  dto.getDate() + dto.getConfirmed() + dto.getDeaths() + dto.getRecovered() + dto.getActive()).hashCode();
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
    private CovidCompleteDto processLine(String line, DateTimeFormatter dateFormatter, int lineIndex) {
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (fields.length < MIN_FIELDS_REQUIRED) {
            logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.", 
                    lineIndex, MIN_FIELDS_REQUIRED, fields.length);
            return null;
        }

        // Extract and clean fields
        String provinceStateName = fields[IDX_PROVINCE_STATE].trim();
        String countryRegionName = fields[IDX_COUNTRY_REGION].trim();
        
        double lat = 0.0, lon = 0.0;
        int confirmed = 0, deaths = 0, recovered = 0, active = 0;
        LocalDate date;
        
        try {
            lat = fields[IDX_LATITUDE].isEmpty() ? 0.0 : Double.parseDouble(fields[IDX_LATITUDE].trim());
            lon = fields[IDX_LONGITUDE].isEmpty() ? 0.0 : Double.parseDouble(fields[IDX_LONGITUDE].trim());
            date = LocalDate.parse(fields[IDX_DATE].trim(), dateFormatter);
            confirmed = fields[IDX_CONFIRMED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_CONFIRMED].trim());
            deaths = fields[IDX_DEATHS].isEmpty() ? 0 : Integer.parseInt(fields[IDX_DEATHS].trim());
            recovered = fields[IDX_RECOVERED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_RECOVERED].trim());
            active = fields[IDX_ACTIVE].isEmpty() ? 0 : Integer.parseInt(fields[IDX_ACTIVE].trim());
        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing numeric fields: {}", lineIndex, e.getMessage());
            return null;
        } catch (DateTimeParseException e) {
            logger.warn("Line {}: Error parsing date: {}", lineIndex, e.getMessage());
            return null;
        }
        
        String whoRegion = fields[IDX_WHO_REGION].trim();

        return new CovidCompleteDto(
                provinceStateName,
                cleanerHelper.cleanRegionName(countryRegionName),
                lat,
                lon,
                date,
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
    private List<DiseaseCase> mapDtosToEntities(HashMap<Integer, CovidCompleteDto> dtoMap) throws MappingException {
        List<DiseaseCase> diseaseCases = new ArrayList<>();
        logger.debug("Converting DTOs to entities...");
        
        for (CovidCompleteDto dto : dtoMap.values()) {
            try {
                DiseaseCase diseaseCase = mapper.toEntity(dto);
                diseaseCases.add(diseaseCase);
            } catch (Exception e) {
                logger.warn("Error mapping DTO to entity: {}", e.getMessage());
                throw new MappingException("Error mapping CovidCompleteDto to entity objects", e);
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
            saveCountries();
            saveRegions();
            saveLocations();
            saveDiseases();
        } catch (DataAccessException e) {
            logger.error("Database error while saving entities: {}", e.getMessage());
            throw new PersistenceException("Error saving COVID Complete data to database", e);
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
