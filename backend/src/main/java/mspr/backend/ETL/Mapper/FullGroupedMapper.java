package mspr.backend.ETL.Mapper;

import mspr.backend.ETL.DTO.FullGroupedDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import mspr.backend.BO.*;
import mspr.backend.ETL.Helpers.*;

@Component
public class FullGroupedMapper {

    // Constants for standard names
    public static final String STANDARD_REGION_NAME = "standard";
    public static final String STANDARD_LOCATION_NAME = "standard";
    public static final String COVID_19_DISEASE_NAME = "COVID-19";

    private final CacheHelper cacheHelper;
    private final CleanerHelper cleanerHelper;
    private final Disease disease;

    @Autowired
    public FullGroupedMapper(CacheHelper cacheHelper, CleanerHelper cleanerHelper) {
        this.cacheHelper = cacheHelper;
        this.cleanerHelper = cleanerHelper;
        // Preload COVID-19 disease
        this.disease = cacheHelper.getOrCreateDisease(COVID_19_DISEASE_NAME);
    }

    /**
     * Converts a FullGroupedDto to a DiseaseCase entity without direct persistence.
     * Builds the Country/Region/Location hierarchy via CacheHelper.
     */
    public DiseaseCase toEntity(FullGroupedDto dto) {
        // Clean country name
        String countryName = cleanerHelper.cleanCountryName(dto.getCountryRegion());
        Country country = cacheHelper.getOrCreateCountry(countryName);

        Region regionStandardFromCountry = cacheHelper.getOrCreateRegion(country, STANDARD_REGION_NAME);

        // No province/state in this dataset, create location directly at country level
        Location location = cacheHelper.getOrCreateLocation(regionStandardFromCountry, STANDARD_LOCATION_NAME);

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
