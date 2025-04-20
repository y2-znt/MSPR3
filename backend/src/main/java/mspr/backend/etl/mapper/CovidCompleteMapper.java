package mspr.backend.etl.mapper;

import mspr.backend.etl.dto.CovidCompleteDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import mspr.backend.entity.*;
import mspr.backend.etl.helpers.*;
import mspr.backend.etl.helpers.cache.CacheManager;

@Component
public class CovidCompleteMapper {

    public static final String COVID_19_DISEASE_NAME = "COVID-19";

    private final CacheManager cacheManager;
    private final CleanerHelper cleanerHelper;
    private final Disease disease;

    @Autowired
    public CovidCompleteMapper(CacheManager cacheManager, CleanerHelper cleanerHelper) {
        this.cacheManager = cacheManager;
        this.cleanerHelper = cleanerHelper;
        // Preload COVID-19 disease to avoid duplication
        this.disease = cacheManager.getOrCreateDisease(COVID_19_DISEASE_NAME);
    }

    /**
     * Converts a CovidCompleteDto to a DiseaseCase entity without saving to database.
     * Uses CacheManager to obtain or create associated Country/Region/Location.
     * 
     * @param dto The DTO to convert
     * @return The mapped DiseaseCase entity or null if country or province is in skip list
     */
    public DiseaseCase toEntity(CovidCompleteDto dto) {
        // Clean country/region names via CleanerHelper
        String countryName = cleanerHelper.cleanCountryName(dto.getCountryRegion());
        String provinceName = dto.getProvinceState();
        
        // Vérifier si le pays est dans la liste à ignorer
        if (cleanerHelper.isInSkipList(countryName)) {
            return null; // Ignorer ce DTO
        }
        
        // Vérifier si la province/état est dans la liste à ignorer (si elle existe)
        if (provinceName != null && !provinceName.isEmpty() && cleanerHelper.isInSkipList(provinceName)) {
            return null; // Ignorer ce DTO
        }
        
        // Créer le pays avec la région WHO (pas de continent disponible dans ce DTO)
        Country country = cacheManager.getOrCreateCountry(countryName, null, dto.getWhoRegion());
        
        Region region;
        Location location;
        
        if (provinceName != null && !provinceName.isEmpty()) {
            // Si une province/état est spécifié, l'utiliser comme Région
            provinceName = cleanerHelper.cleanRegionName(provinceName);
            region = cacheManager.getOrCreateRegion(country, provinceName);
            // Créer une location avec le même nom que la province (elle sera ajustée si nécessaire)
            location = cacheManager.getOrCreateLocationWithEmptyHandling(region, provinceName);
        } else {
            // Si pas de province, utiliser la région standard du pays
            region = cacheManager.getOrCreateRegionWithEmptyHandling(country, null);
            // Et utiliser la location standard de cette région
            location = cacheManager.getOrCreateLocationWithEmptyHandling(region, null);
        }

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
