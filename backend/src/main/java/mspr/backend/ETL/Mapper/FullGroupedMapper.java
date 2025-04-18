package mspr.backend.ETL.Mapper;

import mspr.backend.ETL.DTO.FullGroupedDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import mspr.backend.BO.*;
import mspr.backend.ETL.Helpers.*;

@Component
public class FullGroupedMapper {

    private final CacheHelper cacheHelper;
    private final CleanerHelper cleanerHelper;
    private final Disease disease;

    @Autowired
    public FullGroupedMapper(CacheHelper cacheHelper, CleanerHelper cleanerHelper) {
        this.cacheHelper = cacheHelper;
        this.cleanerHelper = cleanerHelper;
        // Précharger la maladie COVID-19
        this.disease = cacheHelper.getOrCreateDisease("COVID-19");
    }

    /**
     * Convertit un FullGroupedDto en entité DiseaseCase sans persistances directes.
     * Construit la hiérarchie Country/Region/Location via CacheHelper.
     */
    public DiseaseCase toEntity(FullGroupedDto dto) {
        // Nettoyage du nom du pays
        String countryName = cleanerHelper.cleanCountryName(dto.getCountryRegion());
        Country country = cacheHelper.getOrCreateCountry(countryName);

        Region regionStandardFromCountry = cacheHelper.getOrCreateRegion(country, "standard");

        // Pas de province/état dans ce dataset, on crée directement la location au niveau du pays
        Location location = cacheHelper.getOrCreateLocation(regionStandardFromCountry,  "standard");

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
