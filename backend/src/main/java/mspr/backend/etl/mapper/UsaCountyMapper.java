package mspr.backend.etl.mapper;

import mspr.backend.etl.dto.UsaCountyDto;
import mspr.backend.BO.*;
import mspr.backend.etl.helpers.CacheHelper;
import mspr.backend.etl.helpers.CleanerHelper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class UsaCountyMapper {

    public static final String COVID_19_DISEASE_NAME = "COVID-19";
    
    private CleanerHelper cleanerHelper;
    
    @Autowired
    public UsaCountyMapper(CleanerHelper cleanerHelper) {
        this.cleanerHelper = cleanerHelper;
    }

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
        String countryName = dto.getCountryRegion() != null ? dto.getCountryRegion().trim() : null;
        String regionName = dto.getProvinceState() != null ? dto.getProvinceState().trim() : null;
        String locationName = dto.getCounty() != null ? dto.getCounty().trim() : null;
        
        // Pour les US on connaît le continent (Amérique du Nord) et la région WHO (Americas)
        Country country = cache.getOrCreateCountry(countryName, "North America", "Americas");
        
        // Crée la région (ou la récupère si elle existe déjà), en gérant les cas vides
        Region region = cache.getOrCreateRegionWithEmptyHandling(country, regionName);
        
        // Crée la location (ou la récupère si elle existe déjà), en gérant les cas vides
        return cache.getOrCreateLocationWithEmptyHandling(region, locationName);
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



