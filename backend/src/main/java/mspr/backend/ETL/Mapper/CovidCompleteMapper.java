package mspr.backend.ETL.Mapper;

import mspr.backend.ETL.DTO.CovidCompleteDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import mspr.backend.BO.*;
import mspr.backend.ETL.Helpers.*;

@Component
public class CovidCompleteMapper {

    // Constants for standard names
    public static final String STANDARD_LOCATION_NAME = "standard";
    public static final String COVID_19_DISEASE_NAME = "COVID-19";

    private final CacheHelper cacheHelper;
    private final CleanerHelper cleanerHelper;
    private final Disease disease;

    @Autowired
    public CovidCompleteMapper(CacheHelper cacheHelper, CleanerHelper cleanerHelper) {
        this.cacheHelper = cacheHelper;
        this.cleanerHelper = cleanerHelper;
        // Preload COVID-19 disease to avoid duplication
        this.disease = cacheHelper.getOrCreateDisease(COVID_19_DISEASE_NAME);
    }

    /**
     * Converts a CovidCompleteDto to a DiseaseCase entity without saving to database.
     * Uses CacheHelper to obtain or create associated Country/Region/Location.
     */
    public DiseaseCase toEntity(CovidCompleteDto dto) {
        // Clean country/region names via CleanerHelper
        String countryName = cleanerHelper.cleanCountryName(dto.getCountryRegion());
        String provinceName = dto.getProvinceState();
        Region region = null;
        String locationName = STANDARD_LOCATION_NAME;
        
        if (provinceName != null && !provinceName.isEmpty()) {
            // If there's a province/state, consider it as the Region
            provinceName = cleanerHelper.cleanRegionName(provinceName);
            region = cacheHelper.getOrCreateRegion(cacheHelper.getOrCreateCountry(countryName), provinceName);
            locationName = provinceName;
        }
        
        Country country = cacheHelper.getOrCreateCountry(countryName);
        Region regionFromCountry = cacheHelper.getOrCreateRegion(country, countryName);
        Location location = cacheHelper.getOrCreateLocation(regionFromCountry, cleanerHelper.cleanLocationName(locationName));

        // Create main DiseaseCase entity (not persisted here)
        DiseaseCase diseaseCase = new DiseaseCase();
        diseaseCase.setDisease(this.disease);
        diseaseCase.setLocation(location);
        diseaseCase.setDate(dto.getDate());
        diseaseCase.setConfirmedCases(dto.getConfirmed());
        diseaseCase.setDeaths(dto.getDeaths());
        diseaseCase.setRecovered(dto.getRecovered());

        return diseaseCase;
    }
}
