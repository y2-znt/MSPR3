package mspr.backend.etl.service;

import jakarta.transaction.Transactional;
import mspr.backend.entity.Disease;
import mspr.backend.entity.DiseaseCase;
import mspr.backend.etl.dto.CovidCompleteDto;
import mspr.backend.etl.mapper.CovidCompleteMapper;
import mspr.backend.etl.exceptions.*;
import mspr.backend.repository.DiseaseCaseRepository;
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
public class CovidCompleteService extends AbstractCsvImportService<CovidCompleteDto> {

    private final Logger logger = LoggerFactory.getLogger(CovidCompleteService.class);
    private static final String FILE_NAME = "covid_19_clean_complete.csv";

    // CSV field indices
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

    private List<DiseaseCase> diseaseCasesToSave;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void preProcessing() {
        this.diseaseCasesToSave = new ArrayList<>();
        logger.debug("Initialized list for DiseaseCase entities.");
    }

    @Override
    protected CovidCompleteDto processLine(String[] fields, int lineNumber) {
        if (fields.length < MIN_FIELDS_REQUIRED) {
            logger.warn("Line {}: Insufficient fields (expected at least {}, got {}). Skipping line.",
                    lineNumber, MIN_FIELDS_REQUIRED, fields.length);
            return null;
        }

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
            logger.warn("Line {}: Error parsing numeric fields: {}", lineNumber, e.getMessage());
            return null;
        } catch (DateTimeParseException e) {
            logger.warn("Line {}: Error parsing date: {}", lineNumber, e.getMessage());
            return null;
        }

        String whoRegion = fields[IDX_WHO_REGION].trim();

        // Use cleanerHelper from abstract class for country name
        return new CovidCompleteDto(
                provinceStateName, // Province/State name might not need cleaning depending on usage
                cleanerHelper.cleanCountryName(countryRegionName),
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

    @Override
    protected void processDto(CovidCompleteDto dto) throws MappingException {
        try {
            DiseaseCase diseaseCase = mapper.toEntity(dto);
            // Vérifier si l'entité est null avant de l'ajouter à la liste
            if (diseaseCase != null) {
                diseaseCasesToSave.add(diseaseCase);
            } else {
                logger.debug("Mapper returned null entity for DTO: {}, skipping", dto);
            }
        } catch (Exception e) {
            logger.warn("Error mapping CovidCompleteDto to entity: {}", e.getMessage());
            throw new MappingException("Error mapping CovidCompleteDto to entity objects", e);
        }
    }

    @Override
    protected void postProcessing() throws PersistenceException {
        logger.debug("Starting post-processing for CovidCompleteService...");
        // Filtrer les entrées null avant de mettre à jour les références
        diseaseCasesToSave.removeIf(dc -> dc == null);
        
        updateDiseaseCaseReferences(this.diseaseCasesToSave);

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
        this.diseaseCasesToSave.clear();
    }

    private void updateDiseaseCaseReferences(List<DiseaseCase> diseaseCases) {
        logger.debug("Updating references for {} disease cases.", diseaseCases.size());
        int updateErrors = 0;
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
                    logger.warn("Could not find managed Disease entity in cache for name: {} during reference update.", diseaseName);
                    updateErrors++;
                }
            } else {
                logger.warn("DiseaseCase has null Disease, skipping reference update");
            }
            // Location references should be handled by the mapper using CacheManager
        }
         if (updateErrors > 0) {
            logger.warn("Encountered {} errors while updating disease references.", updateErrors);
        }
        logger.debug("Finished updating disease case references.");
    }
}
