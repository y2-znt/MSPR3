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
            for (int l = 1; l < lines.size(); l++) {
                try {
                    String line = lines.get(l);
                    String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    if (fields.length < 16) {
                        logger.warn("Line {}: Insufficient fields (expected at least 16, got {}). Skipping line.", l, fields.length);
                        lineErrors++;
                        continue;
                    }

                    String countryName = fields[0];
                    String continent = fields[1];
                    
                    int population = 0, totalCases = 0, totalDeaths = 0, totalRecovered = 0, activeCases = 0;
                    try {
                        population = fields[2].isEmpty() ? 0 : Integer.parseInt(fields[2]);
                        totalCases = fields[3].isEmpty() ? 0 : Integer.parseInt(fields[3]);
                        totalDeaths = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5]);
                        totalRecovered = fields[7].isEmpty() ? 0 : Integer.parseInt(fields[7]);
                        activeCases = fields[9].isEmpty() ? 0 : Integer.parseInt(fields[9]);
                    } catch (NumberFormatException e) {
                        logger.warn("Line {}: Error parsing numeric fields: {}", l, e.getMessage());
                        lineErrors++;
                        continue;
                    }
                    
                    String whoRegion = fields[15];

                    WorldometerDto dto = new WorldometerDto(countryName, continent, population, totalCases, totalDeaths, totalRecovered, activeCases, whoRegion);
                    int hashKey = (countryName + continent + population + totalCases + totalDeaths + totalRecovered + activeCases).hashCode();
                    dtoMap.put(hashKey, dto);
                } catch (Exception e) {
                    logger.warn("Line {}: Unexpected error processing line: {}", l, e.getMessage());
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
