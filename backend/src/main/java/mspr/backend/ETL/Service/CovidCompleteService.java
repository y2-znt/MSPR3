package mspr.backend.ETL.Service;
import mspr.backend.BO.*;
import mspr.backend.ETL.DTO.CovidCompleteDto;
import mspr.backend.ETL.Helpers.*;
import mspr.backend.ETL.Mapper.CovidCompleteMapper;
import mspr.backend.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

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
public class CovidCompleteService {

    private static final Logger logger = LoggerFactory.getLogger(CovidCompleteService.class);
    public static final String FILE_NAME = "covid_19_clean_complete.csv";

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

            // Read CSV file
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            logger.info("Read {} lines from {}", lines.size(), FILE_NAME);
            
            HashMap<Integer, CovidCompleteDto> dtoMap = new HashMap<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int lineErrors = 0;

            // Skip header
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

                    // Extract and clean fields
                    String provinceStateName = fields[0].trim();
                    String countryRegionName = fields[1].trim();
                    
                    double lat = 0.0, lon = 0.0;
                    int confirmed = 0, deaths = 0, recovered = 0, active = 0;
                    LocalDate date;
                    
                    try {
                        lat = fields[2].isEmpty() ? 0.0 : Double.parseDouble(fields[2].trim());
                        lon = fields[3].isEmpty() ? 0.0 : Double.parseDouble(fields[3].trim());
                        date = LocalDate.parse(fields[4].trim(), dateFormatter);
                        confirmed = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5].trim());
                        deaths = fields[6].isEmpty() ? 0 : Integer.parseInt(fields[6].trim());
                        recovered = fields[7].isEmpty() ? 0 : Integer.parseInt(fields[7].trim());
                        active = fields[8].isEmpty() ? 0 : Integer.parseInt(fields[8].trim());
                    } catch (NumberFormatException e) {
                        logger.warn("Line {}: Error parsing numeric fields: {}", l, e.getMessage());
                        lineErrors++;
                        continue;
                    } catch (DateTimeParseException e) {
                        logger.warn("Line {}: Error parsing date: {}", l, e.getMessage());
                        lineErrors++;
                        continue;
                    }
                    
                    String whoRegion = fields[9].trim();

                    CovidCompleteDto dto = new CovidCompleteDto(
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

                    int hashKey = (provinceStateName + countryRegionName + lat + lon + date + confirmed + deaths + recovered + active).hashCode();
                    dtoMap.put(hashKey, dto);
                } catch (Exception e) {
                    logger.warn("Line {}: Unexpected error processing line: {}", l, e.getMessage());
                    lineErrors++;
                }
            }

            logger.info("Processed {} unique records with {} errors", dtoMap.size(), lineErrors);

            // Convert DTOs to DiseaseCase via mapper
            List<DiseaseCase> diseaseCases = new ArrayList<>();
            logger.debug("Converting DTOs to entities...");
            for (CovidCompleteDto dto : dtoMap.values()) {
                try {
                    DiseaseCase diseaseCase = mapper.toEntity(dto);
                    diseaseCases.add(diseaseCase);
                } catch (Exception e) {
                    logger.warn("Error mapping DTO to entity: {}", e.getMessage());
                }
            }

            // Save cache entities and update persistent instances in cache
            logger.debug("Saving entities to database...");
            
            Map<String, Country> countries = cacheHelper.getCountries();
            logger.debug("Saving {} countries", countries.size());
            countries = countryRepository.saveAll(countries.values())
                        .stream()
                        .collect(Collectors.toMap(Country::getName, country -> country));
            cacheHelper.setCountries(countries);

            Map<String, Region> regions = cacheHelper.getRegions();
            logger.debug("Saving {} regions", regions.size());
            regions = regionRepository.saveAll(regions.values())
                        .stream()
                        .collect(Collectors.toMap(r -> r.getCountry().getName() + "|" + r.getName(), region -> region));
            cacheHelper.setRegions(regions);

            Map<String, Location> locations = cacheHelper.getLocations();
            logger.debug("Saving {} locations", locations.size());
            locations = locationRepository.saveAll(locations.values())
                        .stream()
                        .collect(Collectors.toMap(l -> l.getRegion().getCountry().getName() + "|" + l.getRegion().getName() + "|" + l.getName(), location -> location));
            cacheHelper.setLocations(locations);

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
            logger.info("COVID Complete import completed: {} cases inserted", diseaseCases.size());
            
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
