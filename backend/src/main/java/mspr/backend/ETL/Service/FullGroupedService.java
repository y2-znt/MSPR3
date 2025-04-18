package mspr.backend.ETL.Service;

import jakarta.transaction.Transactional;
import mspr.backend.BO.*;
import mspr.backend.ETL.DTO.FullGroupedDto;
import mspr.backend.ETL.Helpers.*;
import mspr.backend.ETL.Mapper.FullGroupedMapper;
import mspr.backend.ETL.exceptions.*;
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
import java.util.stream.Collectors;

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
            List<String> lines;
            try {
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            } catch (NoSuchFileException e) {
                throw new DataFileNotFoundException(FILE_NAME, e);
            } catch (IOException e) {
                logger.error("IO error reading file {}: {}", FILE_NAME, e.getMessage());
                throw e; // rethrow as it's already a specific exception
            }
            
            logger.info("Read {} lines from {}", lines.size(), FILE_NAME);
            
            HashMap<Integer, FullGroupedDto> dtoMap = new HashMap<>();
            int lineErrors = 0;
            
            // Skip header
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            logger.debug("Processing data lines...");
            
            // Process each line starting from line 1 (after header)
            for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
                try {
                    String line = lines.get(lineIndex);
                    String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    if (fields.length < MIN_FIELDS_REQUIRED) {
                        logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.", 
                                lineIndex, MIN_FIELDS_REQUIRED, fields.length);
                        lineErrors++;
                        continue;
                    }

                    LocalDate date;
                    try {
                        date = LocalDate.parse(fields[IDX_DATE].trim(), dateFormatter);
                    } catch (DateTimeParseException e) {
                        logger.warn("Line {}: Error parsing date: {}", lineIndex, e.getMessage());
                        lineErrors++;
                        continue;
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
                        lineErrors++;
                        continue;
                    }
                    
                    String whoRegion = fields[IDX_WHO_REGION].trim();

                    FullGroupedDto dto = new FullGroupedDto(
                            date,
                            countryRegionName,
                            confirmed,
                            deaths,
                            recovered,
                            active,
                            whoRegion
                    );
                    int hashKey = (date + countryRegionName + confirmed + deaths + recovered + active).hashCode();
                    dtoMap.put(hashKey, dto);
                } catch (Exception e) {
                    logger.warn("Line {}: Unexpected error processing line: {}", lineIndex, e.getMessage());
                    lineErrors++;
                }
            }

            logger.info("Processed {} unique records with {} errors", dtoMap.size(), lineErrors);
         
            // Convert DTOs to DiseaseCase via mapper
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

            try {
                logger.debug("Saving entities to database...");
                
                // Save in batch the cache entities to avoid detached entity errors
                // 1. Countries
                Map<String, Country> countries = cacheHelper.getCountries();
                logger.debug("Saving {} countries", countries.size());
                countries = countryRepository.saveAll(countries.values())
                            .stream()
                            .collect(Collectors.toMap(Country::getName, country -> country));
                cacheHelper.setCountries(countries);

                // 2. Regions
                Map<String, Region> regions = cacheHelper.getRegions();
                logger.debug("Saving {} regions", regions.size());
                regions = regionRepository.saveAll(regions.values())
                            .stream()
                            .collect(Collectors.toMap(r -> r.getCountry().getName() + "|" + r.getName(), region -> region));
                cacheHelper.setRegions(regions);

                // 3. Locations
                Map<String, Location> locations = cacheHelper.getLocations();
                logger.debug("Saving {} locations", locations.size());
                locations = locationRepository.saveAll(locations.values())
                            .stream()
                            .collect(Collectors.toMap(l -> l.getRegion().getCountry().getName() + "|" + l.getRegion().getName() + "|" + l.getName(), location -> location));
                cacheHelper.setLocations(locations);

                // 4. Diseases
                Map<String, Disease> diseases = cacheHelper.getDiseases();
                logger.debug("Saving {} diseases", diseases.size());
                diseases = diseaseRepository.saveAll(diseases.values())
                                .stream()
                                .collect(Collectors.toMap(Disease::getName, disease -> disease));
                cacheHelper.setDiseases(diseases);

                // Set managed disease entities
                for (DiseaseCase dc : diseaseCases) {
                    if (dc.getDisease() != null) {
                        String diseaseName = dc.getDisease().getName();
                        Disease managedDisease = cacheHelper.getDiseases().get(diseaseName);
                        dc.setDisease(managedDisease);
                    }
                }

                // Batch insert all DiseaseCases
                logger.info("Saving {} disease cases", diseaseCases.size());
                diseaseCaseRepository.saveAll(diseaseCases);
            } catch (DataAccessException e) {
                logger.error("Database error while saving entities: {}", e.getMessage());
                throw new PersistenceException("Error saving Full Grouped data to database", e);
            }
            
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
}
