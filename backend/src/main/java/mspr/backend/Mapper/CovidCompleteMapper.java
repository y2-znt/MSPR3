package mspr.backend.Mapper;

import mspr.backend.DTO.CovidCompleteDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import mspr.backend.BO.*;
import mspr.backend.Helpers.*;

@Component
public class CovidCompleteMapper {

    private final CacheHelper cacheHelper;
    private final CleanerHelper cleanerHelper;
    private final Disease disease;

    @Autowired
    public CovidCompleteMapper(CacheHelper cacheHelper, CleanerHelper cleanerHelper) {
        this.cacheHelper = cacheHelper;
        this.cleanerHelper = cleanerHelper;
        // Précharger la maladie COVID-19 pour éviter toute duplication
        this.disease = cacheHelper.getOrCreateDisease("COVID-19");
    }

    /**
     * Convertit un CovidCompleteDto en entité DiseaseCase sans effectuer de sauvegarde en base.
     * Utilise CacheHelper pour obtenir ou créer les Country/Region/Location associés.
     */
    public DiseaseCase toEntity(CovidCompleteDto dto) {
        // Nettoyage des noms de pays/régions via CleanerHelper
        String countryName = cleanerHelper.cleanCountryName(dto.getCountryRegion());
        String provinceName = dto.getProvinceState();
        Region region = null;
        String locationName = "standard";
        if (provinceName != null && !provinceName.isEmpty()) {
            // S'il y a une province/état, on la considère comme Region
            provinceName = cleanerHelper.cleanRegionName(provinceName);
            region = cacheHelper.getOrCreateRegion(cacheHelper.getOrCreateCountry(countryName), provinceName);
            locationName = provinceName;
        }
        Country country = cacheHelper.getOrCreateCountry(countryName);
        Region regionFromCountry = cacheHelper.getOrCreateRegion(country, countryName);
        Location location = cacheHelper.getOrCreateLocation(regionFromCountry, cleanerHelper.cleanLocationName(locationName));

        // Création de l'entité principale DiseaseCase (non persistée ici)
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
