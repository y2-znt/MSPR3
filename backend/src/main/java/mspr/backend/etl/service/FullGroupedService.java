package mspr.backend.etl.service;

import jakarta.transaction.Transactional;
import mspr.backend.BO.Disease;
import mspr.backend.BO.DiseaseCase;
import mspr.backend.etl.dto.FullGroupedDto;
import mspr.backend.etl.mapper.FullGroupedMapper;
import mspr.backend.etl.exceptions.*;
import mspr.backend.Repository.DiseaseCaseRepository;
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

@Service
@Transactional
public class FullGroupedService extends AbstractCsvImportService<FullGroupedDto> {

    private final Logger logger = LoggerFactory.getLogger(FullGroupedService.class);
    private static final String FILE_NAME = "full_grouped.csv";
    
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

    // List to hold entities before final batch save
    private List<DiseaseCase> diseaseCasesToSave;

    // DateTimeFormatter can be static
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void preProcessing() {
        // Initialize the list for this import run
        this.diseaseCasesToSave = new ArrayList<>();
        logger.debug("Initialized list for DiseaseCase entities.");
    }

    /**
     * Processes a single CSV line into a DTO
     *
     * @param fields     Fields from the CSV line
     * @param lineNumber Line number for logging
     * @return DTO or null if the line couldn't be processed
     */
    @Override
    protected FullGroupedDto processLine(String[] fields, int lineNumber) {
        if (fields.length < MIN_FIELDS_REQUIRED) {
            logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.",
                    lineNumber, MIN_FIELDS_REQUIRED, fields.length);
            return null;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(fields[IDX_DATE].trim(), dateFormatter);
        } catch (DateTimeParseException e) {
            logger.warn("Line {}: Error parsing date: {}", lineNumber, e.getMessage());
            return null;
        }

        // Use cleanerHelper from the abstract class
        String countryRegionName = cleanerHelper.cleanRegionName(
                cleanerHelper.cleanCountryName(fields[IDX_COUNTRY_REGION].trim()));

        int confirmed = 0, deaths = 0, recovered = 0, active = 0;
        try {
            confirmed = fields[IDX_CONFIRMED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_CONFIRMED].trim());
            deaths = fields[IDX_DEATHS].isEmpty() ? 0 : Integer.parseInt(fields[IDX_DEATHS].trim());
            recovered = fields[IDX_RECOVERED].isEmpty() ? 0 : Integer.parseInt(fields[IDX_RECOVERED].trim());
            active = fields[IDX_ACTIVE].isEmpty() ? 0 : Integer.parseInt(fields[IDX_ACTIVE].trim());
        } catch (NumberFormatException e) {
            logger.warn("Line {}: Error parsing numeric fields: {}", lineNumber, e.getMessage());
            return null;
        }

        String whoRegion = fields[IDX_WHO_REGION].trim();

        // No need to check for duplicates here, handled by potential database constraints or acceptable
        return new FullGroupedDto(
                date,
                countryRegionName,
                confirmed,
                deaths,
                recovered,
                active,
                whoRegion
        );
    }

    /**
     * Maps the DTO to a DiseaseCase entity and adds it to the list for saving.
     *
     * @param dto The DTO created from a CSV line.
     * @throws MappingException if mapping fails.
     */
    @Override
    protected void processDto(FullGroupedDto dto) throws MappingException {
        try {
            // Mapper uses injected cacheManager implicitly to get/create related entities
            DiseaseCase diseaseCase = mapper.toEntity(dto);
            // Vérifier si l'entité est null avant de l'ajouter à la liste
            if (diseaseCase != null) {
                diseaseCasesToSave.add(diseaseCase);
            } else {
                logger.debug("Mapper returned null entity for DTO: {}, skipping", dto);
            }
        } catch (Exception e) {
            // Catch specific mapping errors if possible, or rethrow as MappingException
            logger.warn("Error mapping FullGroupedDto to entity: {}", e.getMessage());
            throw new MappingException("Error mapping FullGroupedDto to entity objects", e);
        }
    }

    /**
     * Updates references in the collected DiseaseCase entities and saves them.
     */
    @Override
    protected void postProcessing() throws PersistenceException {
        logger.debug("Starting post-processing for FullGroupedService...");
        // Filtrer les entrées null avant de mettre à jour les références
        diseaseCasesToSave.removeIf(dc -> dc == null);
        
        // 1. Update references to managed entities from the cache
        updateDiseaseCaseReferences(this.diseaseCasesToSave);

        // 2. Save the main entities
        if (!diseaseCasesToSave.isEmpty()) {
            logger.info("Saving {} disease cases to the database.", diseaseCasesToSave.size());
            try {
                diseaseCaseRepository.saveAll(diseaseCasesToSave);
                logger.info("Successfully saved {} disease cases.", diseaseCasesToSave.size());
            } catch (DataAccessException e) {
                logger.error("Database error while saving DiseaseCase entities: {}", e.getMessage(), e);
                throw new PersistenceException("Error saving DiseaseCase entities to database", e);
            }
        } else {
            logger.info("No DiseaseCase entities to save.");
        }
        // Clear the list after processing
        this.diseaseCasesToSave.clear();
    }

    /**
     * Updates references in disease cases to managed entities from the cache.
     * Moved from the original service.
     *
     * @param diseaseCases List of disease cases to update
     */
    private void updateDiseaseCaseReferences(List<DiseaseCase> diseaseCases) {
        logger.debug("Updating references for {} disease cases.", diseaseCases.size());
        int updateErrors = 0;
        // Use cacheManager injected from the abstract class
        for (DiseaseCase dc : diseaseCases) {
            // Vérifier si dc est null avant d'y accéder
            if (dc == null) {
                logger.warn("Encountered null DiseaseCase in the list, skipping");
                continue;
            }
            
            if (dc.getDisease() != null) {
                String diseaseName = dc.getDisease().getName();
                Disease managedDisease = cacheManager.getDiseases().get(diseaseName);
                if (managedDisease != null) {
                    dc.setDisease(managedDisease);
                } else {
                    // This case might indicate an issue if a disease was expected but not found/created
                    logger.warn("Could not find managed Disease entity in cache for name: {} during reference update.", diseaseName);
                    updateErrors++;
                }
            } else {
                logger.warn("DiseaseCase has null Disease, skipping reference update");
            }
            // Note: Location references are likely handled during the mapping phase by the mapper using CacheManager
        }
        if (updateErrors > 0) {
            logger.warn("Encountered {} errors while updating disease references.", updateErrors);
        }
        logger.debug("Finished updating disease case references.");
    }
}
