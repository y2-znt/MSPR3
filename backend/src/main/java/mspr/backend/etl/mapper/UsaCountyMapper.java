package mspr.backend.etl.mapper;

import mspr.backend.etl.dto.UsaCountyDto;
import mspr.backend.entity.*;
import mspr.backend.etl.helpers.cache.CacheManager;
import mspr.backend.etl.helpers.CleanerHelper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UsaCountyMapper {

    private static final Logger logger = LoggerFactory.getLogger(UsaCountyMapper.class);
    private static final String COVID_19_DISEASE_NAME = "COVID-19";
    
    private final CleanerHelper cleanerHelper;
    
    @Autowired
    public UsaCountyMapper(CleanerHelper cleanerHelper) {
        this.cleanerHelper = cleanerHelper;
    }

    /**
     * Maps a UsaCountyDto to a DiseaseCase entity.
     * Returns null if the mapping shouldn't be processed (e.g., in skip list).
     *
     * @param dto The UsaCountyDto to map
     * @param cacheManager The CacheManager to use for looking up related entities
     * @return A DiseaseCase entity, or null if it should be skipped
     */
    public DiseaseCase fromDto(UsaCountyDto dto, CacheManager cacheManager) {
        // Sanity checks first
        if (dto == null) {
            logger.warn("Received null DTO, cannot process");
            return null;
        }

        // Check if country, state, or county name is in the skip list
        if (cleanerHelper.isInSkipList(dto.getCountryRegion())) {
            logger.debug("Skipping DTO with country in skip list: {}", dto.getCountryRegion());
            return null;
        }

        if (cleanerHelper.isInSkipList(dto.getProvinceState())) {
            logger.debug("Skipping DTO with province/state in skip list: {}", dto.getProvinceState());
            return null;
        }

        if (cleanerHelper.isInSkipList(dto.getCounty())) {
            logger.debug("Skipping DTO with county in skip list: {}", dto.getCounty());
            return null;
        }

        // Process location data (country, region, location)
        try {
            Location location = processLocationData(dto, cacheManager);
            if (location == null) {
                logger.debug("Could not create or find location for DTO: country={}, province={}, county={}",
                    dto.getCountryRegion(), dto.getProvinceState(), dto.getCounty());
                return null;
            }

            // Get disease from cache (should be COVID-19)
            Disease disease = getDiseaseFromCache(cacheManager);
            
            // Now create the DiseaseCase entity
            return createDiseaseCase(dto, location, disease);
        } catch (Exception e) {
            logger.error("Error mapping DTO to entity: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Processes location data from the DTO.
     * Gets country, region, and location entities from the cache.
     *
     * @param dto The DTO containing location data
     * @param cacheManager The CacheManager to use
     * @return A Location entity
     */
    private Location processLocationData(UsaCountyDto dto, CacheManager cacheManager) {
        String countryName = dto.getCountryRegion();
        if (countryName == null || countryName.isEmpty()) {
            logger.warn("DTO has null or empty country name");
            return null;
        }
        
        // Always USA for this dataset
        if (!countryName.equals("USA")) {
            logger.warn("Expected USA as country, but got: {}", countryName);
        }

        String regionName = dto.getProvinceState();
        if (regionName == null || regionName.isEmpty()) {
            logger.warn("DTO has null or empty region name for country: {}", countryName);
            return null;
        }

        String locationName = dto.getCounty();
        if (locationName == null || locationName.isEmpty()) {
            logger.debug("DTO has null or empty location name for region: {} in country: {}", regionName, countryName);
            return null;
        }

        // Look up or create country
        Country country = cacheManager.getOrCreateCountry(countryName, "NORTH_AMERICA", "Americas");
        if (country == null) {
            logger.warn("Failed to create country: {}", countryName);
            return null;
        }

        // Look up or create region
        Region region = cacheManager.getOrCreateRegion(country, regionName);
        if (region == null) {
            logger.warn("Failed to create region: {} in {}", regionName, countryName);
            return null;
        }

        // Look up or create location (county)
        Location location = cacheManager.getOrCreateLocation(region, locationName);
        if (location == null) {
            logger.warn("Failed to create location: {} in {} ({})", locationName, regionName, countryName);
            return null;
        }
        
        return location;
    }
    
    /**
     * Gets the COVID-19 Disease entity from the cache.
     *
     * @param cacheManager The CacheManager to use
     * @return The COVID-19 Disease entity
     * @throws IllegalStateException if COVID-19 disease is not found in cache
     */
    private Disease getDiseaseFromCache(CacheManager cacheManager) {
        Disease disease = cacheManager.getDiseases().get(COVID_19_DISEASE_NAME);
        if (disease == null) {
            logger.error("COVID-19 disease not found in cache - this should not happen as it should be created in preProcessing");
            throw new IllegalStateException("COVID-19 disease not found in cache");
        }
        return disease;
    }
    
    /**
     * Creates a DiseaseCase entity from the DTO and related entities.
     *
     * @param dto The DTO with case data
     * @param location The Location entity
     * @param disease The Disease entity
     * @return A DiseaseCase entity
     */
    private DiseaseCase createDiseaseCase(UsaCountyDto dto, Location location, Disease disease) {
        DiseaseCase diseaseCase = new DiseaseCase();
        diseaseCase.setLocation(location);
        diseaseCase.setDisease(disease);
        diseaseCase.setDate(dto.getDate());
        diseaseCase.setConfirmedCases(dto.getConfirmed());
        diseaseCase.setDeaths(dto.getDeaths());
        diseaseCase.setRecovered(dto.getRecovered());
        
        logger.debug("Created disease case: date={}, country={}, region={}, location={}, confirmed={}, deaths={}",
                dto.getDate(), location.getRegion().getCountry().getName(), 
                location.getRegion().getName(), location.getName(),
                dto.getConfirmed(), dto.getDeaths());
        
        return diseaseCase;
    }
}



