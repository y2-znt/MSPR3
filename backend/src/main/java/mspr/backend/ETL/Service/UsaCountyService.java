package mspr.backend.ETL.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mspr.backend.BO.*;
import mspr.backend.ETL.DTO.FullGroupedDto;
import mspr.backend.ETL.DTO.UsaCountyDto;
import mspr.backend.ETL.Helpers.*;
import mspr.backend.ETL.Mapper.UsaCountyMapper;
import mspr.backend.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    @Transactional
    public int importData() {
        try {
            // Initialize in-memory cache for reference entities
            CacheHelper cache = new CacheHelper();
            logger.debug("Initialized cache for USA county data import");

            // Read CSV file and convert to DTOs
            List<UsaCountyDto> dtos = new ArrayList<>();
            List<String> lines = new ArrayList<>();
            String pathFile = "src/main/resources/data/" + FILE_NAME;
            Path path = Paths.get(pathFile);

            if (Files.isRegularFile(path) && Files.exists(path)) {
                logger.info("File {} found. Starting import process.", FILE_NAME);
            } else {
                logger.error("File {} does not exist or is not a regular file. Import aborted.", FILE_NAME);
                return 0;
            }

            // Read CSV file
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
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
                        date = LocalDate.parse(fields[11].trim(), dateFmt);
                        confirmed = fields[12].isEmpty() ? 0 : Integer.parseInt(fields[12].trim());
                        deaths = fields[13].isEmpty() ? 0 : Integer.parseInt(fields[13].trim());
                    } catch (NumberFormatException e) {
                        logger.warn("Line {}: Error parsing numeric fields: {}", l, e.getMessage());
                        lineErrors++;
                        continue;
                    } catch (DateTimeParseException e) {
                        logger.warn("Line {}: Error parsing date: {}", l, e.getMessage());
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

            // Process data in batches
            List<DiseaseCase> batchList = new ArrayList<>();
            int batchSize = 1000;
            int totalProcessed = 0;
            int saveCount = 0;
            
            logger.debug("Converting DTOs to entities...");
            for (UsaCountyDto dto : dtos) {
                try {
                    // Use mapper to convert DTO to DiseaseCase entity with cache references
                    DiseaseCase diseaseCase = usaCountyMapper.fromDto(dto, cache);

                    // If a new reference entity was created, save it immediately (to get an ID)
                    Country country = diseaseCase.getLocation().getRegion().getCountry();
                    if (country.getId() == null) {
                        countryRepository.save(country);
                        logger.trace("Saved new country: {}", country.getName());
                    }
                    
                    Region region = diseaseCase.getLocation().getRegion();
                    if (region.getId() == null) {
                        regionRepository.save(region);
                        logger.trace("Saved new region: {}", region.getName());
                    }
                    
                    Location location = diseaseCase.getLocation();
                    if (location.getId() == null) {
                        locationRepository.save(location);
                        logger.trace("Saved new location: {} in {}", location.getName(), location.getRegion().getName());
                    }

                    batchList.add(diseaseCase);
                    totalProcessed++;
                    
                    if (batchList.size() >= batchSize) {
                        // Batch save DiseaseCases
                        logger.debug("Saving batch of {} disease cases", batchList.size());
                        diseaseCaseRepository.saveAll(batchList);
                        entityManager.flush();
                        entityManager.clear();
                        saveCount += batchList.size();
                        batchList.clear();
                    }
                } catch (Exception e) {
                    logger.warn("Error processing DTO: {}", e.getMessage());
                }
            }
            
            // Insert any remaining records if list is not empty
            if (!batchList.isEmpty()) {
                logger.debug("Saving final batch of {} disease cases", batchList.size());
                diseaseCaseRepository.saveAll(batchList);
                entityManager.flush();
                entityManager.clear();
                saveCount += batchList.size();
                batchList.clear();
            }
            
            logger.info("USA county data import completed: {} records processed, {} disease cases inserted", 
                    totalProcessed, saveCount);
            
            return (lines.size()-1);
        } catch (IOException e) {
            logger.error("IO error reading file {}: {}", FILE_NAME, e.getMessage(), e);
            return 0;
        } catch (Exception e) {
            logger.error("Unexpected error during import of {}: {}", FILE_NAME, e.getMessage(), e);
            return 0;
        }
    }
}


