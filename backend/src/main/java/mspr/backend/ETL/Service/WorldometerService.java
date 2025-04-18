package mspr.backend.ETL.Service;

import jakarta.transaction.Transactional;
import mspr.backend.BO.Country;
import mspr.backend.BO.Region;
import mspr.backend.BO.Location;
import mspr.backend.ETL.DTO.WorldometerDto;
import mspr.backend.ETL.Mapper.WorldometerMapper;
import mspr.backend.ETL.Mapper.WorldometerMapper.CountryRegionLocation;
import mspr.backend.ETL.exceptions.*;
import mspr.backend.Repository.CountryRepository;
import mspr.backend.Repository.RegionRepository;
import mspr.backend.Repository.LocationRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    /**
     * Import data from Worldometer CSV file
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
            
            HashMap<Integer, WorldometerDto> dtoMap = new HashMap<>();
            int lineErrors = 0;

            // Skip header
            logger.debug("Processing data lines...");
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

                    String countryName = fields[IDX_COUNTRY_NAME];
                    String continent = fields[IDX_CONTINENT];
                    
                    int population = 0, totalCases = 0, totalDeaths = 0, totalRecovered = 0, activeCases = 0;
                    try {
                        population = fields[IDX_POPULATION].isEmpty() ? 0 : Integer.parseInt(fields[IDX_POPULATION]);
                        totalCases = fields[IDX_TOTAL_CASES].isEmpty() ? 0 : Integer.parseInt(fields[IDX_TOTAL_CASES]);
                        totalDeaths = fields[IDX_TOTAL_DEATHS].isEmpty() ? 0 : Integer.parseInt(fields[IDX_TOTAL_DEATHS]);
                        totalRecovered = fields[IDX_TOTAL_RECOVERED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_TOTAL_RECOVERED]);
                        activeCases = fields[IDX_ACTIVE_CASES].isEmpty() ? 0 : Integer.parseInt(fields[IDX_ACTIVE_CASES]);
                    } catch (NumberFormatException e) {
                        logger.warn("Line {}: Error parsing numeric fields: {}", lineIndex, e.getMessage());
                        lineErrors++;
                        continue;
                    }
                    
                    String whoRegion = fields[IDX_WHO_REGION];

                    WorldometerDto dto = new WorldometerDto(countryName, continent, population, totalCases, totalDeaths, totalRecovered, activeCases, whoRegion);
                    int hashKey = (countryName + continent + population + totalCases + totalDeaths + totalRecovered + activeCases).hashCode();
                    dtoMap.put(hashKey, dto);
                } catch (Exception e) {
                    logger.warn("Line {}: Unexpected error processing line: {}", lineIndex, e.getMessage());
                    lineErrors++;
                }
            }

            logger.info("Processed {} unique records with {} errors", dtoMap.size(), lineErrors);

            // Collect unique entities through mapper
            List<Country> countries = new ArrayList<>();
            List<Region> regions = new ArrayList<>();
            List<Location> locations = new ArrayList<>();
            
            logger.debug("Converting DTOs to entities...");
            for (WorldometerDto dto : dtoMap.values()) {
                try {
                    CountryRegionLocation triple = mapper.toEntity(dto);

                    Country country = triple.getCountry();
                    if (!countries.contains(country)) {
                        countries.add(country);
                    }
                    // Region may be null if not provided in the file
                    if (triple.getRegion() != null && !regions.contains(triple.getRegion())) {
                        regions.add(triple.getRegion());
                    }
                    if (triple.getLocation() != null && !locations.contains(triple.getLocation())) {
                        locations.add(triple.getLocation());
                    }
                } catch (Exception e) {
                    logger.warn("Error mapping DTO to entity: {}", e.getMessage());
                    throw new MappingException("Error mapping WorldometerDto to entity objects", e);
                }
            }

            try {
                // Batch insert collected entities
                logger.debug("Saving {} countries to database", countries.size());
                countryRepository.saveAll(countries);
                
                logger.debug("Saving {} regions to database", regions.size());
                regionRepository.saveAll(regions);
                
                logger.debug("Saving {} locations to database", locations.size());
                locationRepository.saveAll(locations);
            } catch (DataAccessException e) {
                logger.error("Database error while saving entities: {}", e.getMessage());
                throw new PersistenceException("Error saving Worldometer data to database", e);
            }

            logger.info("Worldometer import completed: {} countries, {} regions, and {} locations inserted", 
                    countries.size(), regions.size(), locations.size());

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
