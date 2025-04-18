package mspr.backend.ETL.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mspr.backend.BO.*;
import mspr.backend.ETL.DTO.FullGroupedDto;
import mspr.backend.ETL.DTO.UsaCountyDto;
import mspr.backend.ETL.Helpers.*;
import mspr.backend.ETL.Mapper.UsaCountyMapper;
import mspr.backend.ETL.exceptions.*;
import mspr.backend.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import jakarta.transaction.Transactional;

@Service
public class UsaCountyService {

    private static final Logger logger = LoggerFactory.getLogger(UsaCountyService.class);

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private LocationRepository locationRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UsaCountyMapper usaCountyMapper;


    public static final String FILE_NAME = "usa_county_wise.csv";

    /**
     * Import data from USA county CSV file
     * 
     * @return Number of lines processed from the file
     * @throws DataFileNotFoundException If the required data file is not found
     * @throws IOException If there's an error reading the file
     * @throws PersistenceException If there's an error saving data to the database
     * @throws EtlException For other ETL-related errors
     */
    @Transactional
    public int importData() throws DataFileNotFoundException, IOException, PersistenceException, EtlException {
        try {
            // Initialize in-memory cache for reference entities
            CacheHelper cache = new CacheHelper();
            logger.debug("Initialized cache for USA county data import");

            // Read CSV file and convert to DTOs
            List<UsaCountyDto> dtos = new ArrayList<>();
            List<String> lines;
            String pathFile = "src/main/resources/data/" + FILE_NAME;
            Path path = Paths.get(pathFile);

            // Check if file exists first
            if (!Files.isRegularFile(path) || !Files.exists(path)) {
                logger.error("File {} does not exist or is not a regular file. Import aborted.", FILE_NAME);
                throw new DataFileNotFoundException(FILE_NAME);
            }

            logger.info("File {} found. Starting import process.", FILE_NAME);

            // Read CSV file
            try {
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            } catch (NoSuchFileException e) {
                throw new DataFileNotFoundException(FILE_NAME, e);
            } catch (IOException e) {
                logger.error("IO error reading file {}: {}", FILE_NAME, e.getMessage());
                throw e; // rethrow as it's already a specific exception
            }

            logger.info("Read {} lines from {}", lines.size(), FILE_NAME);
            
            // Define date format such as "M/d/yy" (e.g. "1/22/20")
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("M/d/yy");
            int lineErrors = 0;

            // Skip header
            logger.debug("Processing data lines...");
            for (int l = 1; l < lines.size(); l++) {
                try {
                    String line = lines.get(l);
                    // Regular expression to properly handle commas in quoted fields
                    String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    if (fields.length < 14) {
                        logger.warn("Line {}: Insufficient fields (expected at least 14, got {}). Skipping line.", l, fields.length);
                        lineErrors++;
                        continue;
                    }

                    // Expected indices (according to usa_county_wise.csv order):
                    // 5: Admin2 (county name), 6: Province_State, 7: Country_Region,
                    // 8: Lat, 9: Long_, 11: Date, 12: Confirmed, 13: Deaths
                    String county = fields[5].trim();
                    String provinceState = fields[6].trim();
                    String countryRegion = fields[7].trim();
                    
                    double lat = 0.0, lon = 0.0;
                    LocalDate date;
                    int confirmed = 0, deaths = 0;
                    
                    try {
                        lat = Double.parseDouble(fields[8].trim());
                        lon = Double.parseDouble(fields[9].trim());
                    } catch (NumberFormatException e) {
                        logger.warn("Line {}: Error parsing geographic coordinates: {}", l, e.getMessage());
                        lineErrors++;
                        continue;
                    }
                    
                    try {
                        date = LocalDate.parse(fields[11].trim(), dateFmt);
                    } catch (DateTimeParseException e) {
                        logger.warn("Line {}: Error parsing date: {}", l, e.getMessage());
                        lineErrors++;
                        continue;
                    }
                    
                    try {
                        confirmed = fields[12].isEmpty() ? 0 : Integer.parseInt(fields[12].trim());
                        deaths = fields[13].isEmpty() ? 0 : Integer.parseInt(fields[13].trim());
                    } catch (NumberFormatException e) {
                        logger.warn("Line {}: Error parsing numeric fields: {}", l, e.getMessage());
                        lineErrors++;
                        continue;
                    }
                    
                    // "recovered" and "active" data are not present in this file
                    int recovered = 0;
                    int active = 0;

                    UsaCountyDto dto = new UsaCountyDto(county, provinceState, countryRegion, lat, lon, date, confirmed, deaths, recovered, active);
                    dtos.add(dto);
                } catch (Exception e) {
                    logger.warn("Line {}: Unexpected error processing line: {}", l, e.getMessage());
                    lineErrors++;
                }
            }

            logger.info("Processed {} records with {} errors", dtos.size(), lineErrors);

            try {
                // Process data in batches
                List<DiseaseCase> batchList = new ArrayList<>();
                int batchSize = 1000;
                int totalProcessed = 0;
                int saveCount = 0;
                int mappingErrors = 0;
                
                logger.debug("Converting DTOs to entities...");
                for (UsaCountyDto dto : dtos) {
                    try {
                        // Use mapper to convert DTO to DiseaseCase entity with cache references
                        DiseaseCase diseaseCase = usaCountyMapper.fromDto(dto, cache);

                        // If a new reference entity was created, save it immediately (to get an ID)
                        Country country = diseaseCase.getLocation().getRegion().getCountry();
                        if (country.getId() == null) {
                            try {
                                countryRepository.save(country);
                                logger.trace("Saved new country: {}", country.getName());
                            } catch (DataAccessException e) {
                                logger.error("Error saving country {}: {}", country.getName(), e.getMessage());
                                throw new PersistenceException("Error saving country entity", e);
                            }
                        }
                        
                        Region region = diseaseCase.getLocation().getRegion();
                        if (region.getId() == null) {
                            try {
                                regionRepository.save(region);
                                logger.trace("Saved new region: {}", region.getName());
                            } catch (DataAccessException e) {
                                logger.error("Error saving region {}: {}", region.getName(), e.getMessage());
                                throw new PersistenceException("Error saving region entity", e);
                            }
                        }
                        
                        Location location = diseaseCase.getLocation();
                        if (location.getId() == null) {
                            try {
                                locationRepository.save(location);
                                logger.trace("Saved new location: {} in {}", location.getName(), location.getRegion().getName());
                            } catch (DataAccessException e) {
                                logger.error("Error saving location {}: {}", location.getName(), e.getMessage());
                                throw new PersistenceException("Error saving location entity", e);
                            }
                        }

                        batchList.add(diseaseCase);
                        totalProcessed++;
                        
                        if (batchList.size() >= batchSize) {
                            // Batch save DiseaseCases
                            logger.debug("Saving batch of {} disease cases", batchList.size());
                            try {
                                diseaseCaseRepository.saveAll(batchList);
                                entityManager.flush();
                                entityManager.clear();
                                saveCount += batchList.size();
                                batchList.clear();
                            } catch (DataAccessException e) {
                                logger.error("Error saving batch of disease cases: {}", e.getMessage());
                                throw new PersistenceException("Error saving batch of disease cases", e);
                            }
                        }
                    } catch (PersistenceException e) {
                        throw e; // propagate persistence exceptions
                    } catch (Exception e) {
                        logger.warn("Error processing DTO: {}", e.getMessage());
                        mappingErrors++;
                    }
                }
                
                // Insert any remaining records if list is not empty
                if (!batchList.isEmpty()) {
                    logger.debug("Saving final batch of {} disease cases", batchList.size());
                    try {
                        diseaseCaseRepository.saveAll(batchList);
                        entityManager.flush();
                        entityManager.clear();
                        saveCount += batchList.size();
                        batchList.clear();
                    } catch (DataAccessException e) {
                        logger.error("Error saving final batch of disease cases: {}", e.getMessage());
                        throw new PersistenceException("Error saving final batch of disease cases", e);
                    }
                }
                
                logger.info("USA county data import completed: {} records processed, {} disease cases inserted, {} mapping errors", 
                        totalProcessed, saveCount, mappingErrors);
                
                return (lines.size()-1);
            } catch (PersistenceException e) {
                throw e; // propagate specific persistence exceptions
            } catch (Exception e) {
                logger.error("Error during USA county data processing: {}", e.getMessage());
                throw new EtlException("Error during USA county data processing", e);
            }
        } catch (DataFileNotFoundException | IOException | PersistenceException e) {
            // Let these specific exceptions propagate
            throw e;
        } catch (Exception e) {
            // Wrap any other exceptions
            logger.error("Unexpected error during import of {}: {}", FILE_NAME, e.getMessage(), e);
            throw new EtlException("Unexpected error during import of " + FILE_NAME, e);
        }
    }
}


