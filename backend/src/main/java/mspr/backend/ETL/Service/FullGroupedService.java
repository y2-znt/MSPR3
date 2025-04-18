package mspr.backend.ETL.Service;

import jakarta.transaction.Transactional;
import mspr.backend.BO.*;
import mspr.backend.ETL.DTO.FullGroupedDto;
import mspr.backend.ETL.Helpers.*;
import mspr.backend.ETL.Mapper.FullGroupedMapper;
import mspr.backend.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FullGroupedService {

    private static final Logger logger = LoggerFactory.getLogger(FullGroupedService.class);
    public static final String FILE_NAME = "full_grouped.csv";

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

    public int importData() throws Exception {
        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        try {
            if (Files.isRegularFile(path) && Files.exists(path)) {
                logger.info("File {} found. Starting import process.", FILE_NAME);
            } else {
                logger.error("File {} does not exist or is not a regular file. Import aborted.", FILE_NAME);
                return 0;
            }

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            logger.info("Read {} lines from {}", lines.size(), FILE_NAME);
            
            HashMap<Integer, FullGroupedDto> dtoMap = new HashMap<>();
            int lineErrors = 0;
            
            // Skip header
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            logger.debug("Processing data lines...");
            
            for (int l = 1; l < lines.size(); l++) {
                try {
                    String line = lines.get(l);
                    String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    if (fields.length < 10) {
                        logger.warn("Line {}: Insufficient fields (expected at least 10, got {}). Skipping line.", l, fields.length);
                        lineErrors++;
                        continue;
                    }

                    LocalDate date = LocalDate.parse(fields[0].trim(), dateFormatter);
                    String countryRegionName = cleanerHelper.cleanRegionName(cleanerHelper.cleanCountryName(fields[1].trim()));
                    
                    int confirmed = 0, deaths = 0, recovered = 0, active = 0;
                    try {
                        confirmed = fields[2].isEmpty() ? 0 : Integer.parseInt(fields[2].trim());
                        deaths = fields[3].isEmpty() ? 0 : Integer.parseInt(fields[3].trim());
                        recovered = fields[4].isEmpty() ? 0 : Integer.parseInt(fields[4].trim());
                        active = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5].trim());
                    } catch (NumberFormatException e) {
                        logger.warn("Line {}: Error parsing numeric fields: {}", l, e.getMessage());
                        lineErrors++;
                        continue;
                    }
                    
                    String whoRegion = fields[9].trim();

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
                } catch (DateTimeParseException e) {
                    logger.warn("Line {}: Error parsing date: {}", l, e.getMessage());
                    lineErrors++;
                } catch (Exception e) {
                    logger.warn("Line {}: Unexpected error processing line: {}", l, e.getMessage());
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
                }
            }

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
            logger.info("Full Grouped import completed: {} cases inserted", diseaseCases.size());
            
            return (lines.size()-1);
        } catch (IOException e) {
            logger.error("IO error reading file {}: {}", FILE_NAME, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during import of {}: {}", FILE_NAME, e.getMessage(), e);
            throw e;
        }
    }
}
