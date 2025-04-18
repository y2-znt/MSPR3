package mspr.backend.etl.mapper;

import mspr.backend.etl.dto.UsaCountyDto;
import mspr.backend.BO.*;
import mspr.backend.etl.helpers.CacheHelper;
import org.springframework.stereotype.Component;


@Component
public class UsaCountyMapper {

    // Constants for standard names
    public static final String DEFAULT_EMPTY_NAME = "";
    public static final String COVID_19_DISEASE_NAME = "COVID-19";

    /**
     * Converts a UsaCountyDto to a DiseaseCase entity using the provided cache
     * 
     * @param dto The DTO to convert
     * @param cache The cache helper to use for entity creation/retrieval
     * @return The mapped DiseaseCase entity
     */
    public DiseaseCase fromDto(UsaCountyDto dto, CacheHelper cache) {
        if (dto == null) {
            return null;
        }
        
        // Process location data
        Location location = processLocationData(dto, cache);
        
        // Get the Disease entity from cache
        Disease disease = getDiseaseFromCache(cache);

        // Create DiseaseCase entity
        return createDiseaseCase(dto, location, disease);
    }
    
    /**
     * Processes location data from DTO and returns a Location entity
     * 
     * @param dto The DTO containing location data
     * @param cache The cache helper
     * @return The Location entity
     */
    private Location processLocationData(UsaCountyDto dto, CacheHelper cache) {
        // Clean country/region/location names (trim and aliases)
        String countryName = dto.getCountryRegion() != null ? dto.getCountryRegion().trim() : DEFAULT_EMPTY_NAME;
        String regionName = dto.getProvinceState() != null ? dto.getProvinceState().trim() : DEFAULT_EMPTY_NAME;
        String locationName = dto.getCounty() != null ? dto.getCounty().trim() : DEFAULT_EMPTY_NAME;
        
        // If location (county) field is empty, use region name as location
        if (locationName.isEmpty()) {
            locationName = regionName;
        }

        // Get or create reference entities from cache
        Country country = cache.getOrCreateCountry(countryName);
        Region region = cache.getOrCreateRegion(country, regionName);
        return cache.getOrCreateLocation(region, locationName);
    }
    
    /**
     * Gets the Disease entity from cache
     * 
     * @param cache The cache helper
     * @return The Disease entity
     * @throws IllegalStateException if COVID-19 disease is not found in cache
     */
    private Disease getDiseaseFromCache(CacheHelper cache) {
        Disease disease = cache.getDiseases().get(COVID_19_DISEASE_NAME);
        if (disease == null) {
            throw new IllegalStateException("COVID-19 disease not found in cache. Ensure it's added before mapping.");
        }
        return disease;
    }
    
    /**
     * Creates a DiseaseCase entity from the DTO and related entities
     * 
     * @param dto The DTO with disease case data
     * @param location The Location entity
     * @param disease The Disease entity
     * @return The DiseaseCase entity
     */
    private DiseaseCase createDiseaseCase(UsaCountyDto dto, Location location, Disease disease) {
        DiseaseCase diseaseCase = new DiseaseCase();
        diseaseCase.setDisease(disease);
        diseaseCase.setLocation(location);
        diseaseCase.setDate(dto.getDate());
        diseaseCase.setConfirmedCases(dto.getConfirmed());
        diseaseCase.setDeaths(dto.getDeaths());
        diseaseCase.setRecovered(dto.getRecovered());
        
        return diseaseCase;
    }
}



