package mspr.backend.etl.service;

import mspr.backend.entity.*;
import mspr.backend.etl.dto.UsaCountyDto;
import mspr.backend.etl.mapper.UsaCountyMapper;
import mspr.backend.etl.exceptions.*;
import mspr.backend.repository.DiseaseCaseRepository;
import mspr.backend.repository.DiseaseRepository;
import mspr.backend.etl.helpers.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsaCountyService extends AbstractCsvImportService<UsaCountyDto> {

    private final Logger logger = LoggerFactory.getLogger(UsaCountyService.class);
    private static final String FILE_NAME = "usa_county_wise.csv";
    private static final String COVID_19_DISEASE_NAME = "COVID-19";
    private static final int BATCH_SIZE = 25000; // Augmenté pour de meilleures performances

    // CSV field indices
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
    private UsaCountyMapper usaCountyMapper;

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;

    @Autowired
    private DiseaseRepository diseaseRepository;
    
    @Autowired
    private CacheManager cacheManager;

    private List<DiseaseCase> diseaseCasesToSave;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");
    
    // Stocker la référence COVID pour éviter des recherches répétées
    private Disease covidDisease;

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void preProcessing() throws PersistenceException {
        this.diseaseCasesToSave = new ArrayList<>();
        logger.debug("Initialized list for DiseaseCase entities.");
        ensureCovidDiseaseExists();
        // Stocker la référence qui sera réutilisée
        this.covidDisease = cacheManager.getDiseases().get(COVID_19_DISEASE_NAME);
    }

    /**
     * Ensures the COVID-19 disease exists in the database and cache.
     * Kept as private helper method.
     *
     * @throws PersistenceException if there is an error persisting the disease entity
     */
    private void ensureCovidDiseaseExists() throws PersistenceException {
        Disease covidDisease = cacheManager.getDiseases().get(COVID_19_DISEASE_NAME);
        if (covidDisease == null) {
            logger.debug("COVID-19 disease not found in cache, checking database...");
            covidDisease = diseaseRepository.findByName(COVID_19_DISEASE_NAME);
            if (covidDisease == null) {
                logger.info("COVID-19 disease not found in database, creating new entry.");
                covidDisease = new Disease();
                covidDisease.setName(COVID_19_DISEASE_NAME);
                try {
                    covidDisease = diseaseRepository.save(covidDisease);
                    logger.info("Successfully created and saved COVID-19 disease entity.");
                } catch (DataAccessException e) {
                    logger.error("Failed to save COVID-19 disease: {}", e.getMessage(), e);
                    throw new PersistenceException("Failed to save essential COVID-19 disease entity", e);
                }
            }
            cacheManager.addDiseaseToCache(COVID_19_DISEASE_NAME, covidDisease);
            logger.debug("Ensured COVID-19 disease is in cache with ID: {}", covidDisease.getId());
        } else {
            logger.debug("COVID-19 disease already present in cache.");
        }
    }

    /**
     * Processes a single CSV line (as fields) into a DTO.
     * Overrides the abstract method.
     *
     * @param fields Fields from the CSV line
     * @param lineNumber Line number for logging
     * @return DTO or null if the line couldn't be processed
     */
    @Override
    protected UsaCountyDto processLine(String[] fields, int lineNumber) {
        if (fields.length < MIN_FIELDS_REQUIRED) {
            logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.",
                    lineNumber, MIN_FIELDS_REQUIRED, fields.length);
            return null;
        }

        String county = fields[IDX_COUNTY].trim();
        String provinceState = fields[IDX_PROVINCE_STATE].trim();
        String countryRegion = fields[IDX_COUNTRY_REGION].trim();
        double lat = 0.0, lon = 0.0;
        LocalDate date;
        int confirmed = 0, deaths = 0;

        try {
            String latStr = fields[IDX_LATITUDE].trim();
            String lonStr = fields[IDX_LONGITUDE].trim();
            lat = latStr.isEmpty() ? 0.0 : Double.parseDouble(latStr);
            lon = lonStr.isEmpty() ? 0.0 : Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing geographic coordinates: {}. Setting to 0.", lineNumber, e.getMessage());
            // Continue with lat/lon as 0
        }

        try {
            date = LocalDate.parse(fields[IDX_DATE].trim(), dateFormatter);
        } catch (DateTimeParseException e) {
            logger.warn("Line {}: Error parsing date: {}. Skipping line.", lineNumber, e.getMessage());
            return null;
        }

        try {
            confirmed = fields[IDX_CONFIRMED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_CONFIRMED].trim());
            deaths = fields[IDX_DEATHS].isEmpty() ? 0 : Integer.parseInt(fields[IDX_DEATHS].trim());
        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing confirmed/deaths counts: {}. Setting to 0.", lineNumber, e.getMessage());
            // Continue with counts as 0
        }

        // Recovered and Active are not in this file
        int recovered = 0;
        int active = 0;

        countryRegion = cleanerHelper.cleanCountryName(countryRegion);
        provinceState = cleanerHelper.cleanRegionName(provinceState);
        // County name might not need cleaning, depends on requirements

        return new UsaCountyDto(county, provinceState, countryRegion, lat, lon, date, confirmed, deaths, recovered, active);
    }

    /**
     * Processes a DTO: maps it to an entity and adds it to the save list.
     * Overrides the abstract method.
     *
     * @param dto The DTO to process.
     * @throws MappingException If mapping fails.
     */
    @Override
    protected void processDto(UsaCountyDto dto) throws MappingException {
        try {
            DiseaseCase diseaseCase = usaCountyMapper.fromDto(dto, cacheManager);
            if (diseaseCase != null) {
                diseaseCasesToSave.add(diseaseCase);
            } else {
                logger.debug("Mapping DTO resulted in null entity for DTO: {}", dto);
            }
        } catch (IllegalStateException e) {
            logger.error("Mapping error (likely missing COVID-19 disease): {}", e.getMessage());
            throw new MappingException("Mapping error, potentially missing prerequisite data like COVID-19 disease", e);
        } catch (Exception e) {
            logger.warn("Error mapping UsaCountyDto to entity: {}", e.getMessage(), e);
            throw new MappingException("Error mapping UsaCountyDto to entity", e);
        }
    }

    /**
     * Final steps: update references and save main entities.
     * Overrides the abstract method.
     */
    @Override
    protected void postProcessing() throws PersistenceException, EtlException {
        logger.info("Starting post-processing for UsaCountyService.");

        logger.info("Filtering disease cases...");
        // Filtrage plus direct et efficace des entrées null
        diseaseCasesToSave.removeIf(Objects::isNull);
        
        if (diseaseCasesToSave.isEmpty()) {
            logger.info("No DiseaseCase entities to process.");
            return;
        }

        logger.info("Updating disease cases references...");
        updateDiseaseCaseReferences(this.diseaseCasesToSave);

        if (!diseaseCasesToSave.isEmpty()) {
            int totalSize = diseaseCasesToSave.size();
            logger.info("Saving {} disease cases to the database in batches of {}", totalSize, BATCH_SIZE);
            
            try {
                for (int i = 0; i < totalSize; i += BATCH_SIZE) {
                    int endIndex = Math.min(i + BATCH_SIZE, totalSize);
                    List<DiseaseCase> batch = diseaseCasesToSave.subList(i, endIndex);
                    
                    diseaseCaseRepository.saveAll(batch);
                    logger.info("Saved batch {}/{} ({} records)", 
                               (i/BATCH_SIZE)+1, 
                               (int)Math.ceil((double)totalSize/BATCH_SIZE), 
                               batch.size());
                }
                
                logger.info("Successfully saved all {} disease cases.", totalSize);
            } catch (DataAccessException e) {
                logger.error("Database error while saving DiseaseCase entities: {}", e.getMessage(), e);
                throw new PersistenceException("Error saving DiseaseCase entities to database", e);
            }
        } else {
            logger.info("No DiseaseCase entities to save.");
        }
        this.diseaseCasesToSave.clear();
    }

    /**
     * Updates DiseaseCase references to managed entities from the cache.
     * Includes specific logic for Location and ensures COVID-19 is set.
     *
     * @param diseaseCases List of disease cases to update
     * @throws EtlException if there's an unexpected error during update
     */
    private void updateDiseaseCaseReferences(List<DiseaseCase> diseaseCases) throws EtlException {
        logger.debug("Updating DiseaseCase references for {} cases...", diseaseCases.size());
        
        // Vérifier que la référence COVID est disponible
        if (covidDisease == null) {
            covidDisease = cacheManager.getDiseases().get(COVID_19_DISEASE_NAME);
            if (covidDisease == null) {
                logger.error("CRITICAL: COVID-19 disease reference missing from cache during final update.");
                throw new EtlException("COVID-19 disease reference missing unexpectedly during postProcessing");
            }
        }
        
        for (DiseaseCase dc : diseaseCases) {
            if (dc == null) continue;
            
            // Affecter directement la référence COVID
            dc.setDisease(covidDisease);
            
            // Mise à jour optimisée de la référence de localisation
            updateLocationReference(dc);
        }
        
        logger.debug("Finished updating disease case references.");
    }

    /**
     * Updates the location reference using managed entities from the cache.
     * Optimisé pour réduire les vérifications imbriquées.
     *
     * @param dc Disease case to update
     */
    private void updateLocationReference(DiseaseCase dc) {
        Location originalLocation = dc.getLocation();
        if (originalLocation == null) return;
        
        Region originalRegion = originalLocation.getRegion();
        if (originalRegion == null) return;
        
        Country originalCountry = originalRegion.getCountry();
        if (originalCountry == null || originalCountry.getName() == null || 
            originalRegion.getName() == null || originalLocation.getName() == null) return;
        
        String locationKey = cacheManager.getLocationKey(
            originalCountry.getName(), 
            originalRegion.getName(), 
            originalLocation.getName()
        );
        
        Location managedLocation = cacheManager.getLocations().get(locationKey);
        if (managedLocation != null) {
            dc.setLocation(managedLocation);
        }
    }
}


