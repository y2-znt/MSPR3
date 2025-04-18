package mspr.backend.ETL.Mapper;

import mspr.backend.ETL.DTO.UsaCountyDto;
import mspr.backend.BO.*;
import mspr.backend.ETL.Helpers.CacheHelper;
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
        Location location = cache.getOrCreateLocation(region, locationName);
        
        // Get the Disease entity from cache - this should be pre-loaded and persisted
        Disease disease = cache.getDiseases().get(COVID_19_DISEASE_NAME);
        if (disease == null) {
            throw new IllegalStateException("COVID-19 disease not found in cache. Ensure it's added before mapping.");
        }

        // Map DTO to DiseaseCase entity
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



