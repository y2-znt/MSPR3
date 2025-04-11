package mspr.backend.Mapper;

import mspr.backend.DTO.CovidCompleteDto;
import mspr.backend.BO.*;
import mspr.backend.Service.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Component
public class CovidCompleteMapper {

    @Autowired private CountryService countryService;
    @Autowired private RegionService regionService;
    @Autowired private LocationService locationService;
    @Autowired private DiseaseService diseaseService;

    /**
     * Convertit un DTO CovidCompleteDto en un objet DiseaseCase (prêt à être persisté).
     * Cette méthode gère la création/recherche de Country, Region, Location, et lie la Disease (COVID-19).
     */
    public DiseaseCase dtoToEntity(CovidCompleteDto dto) {
        // 1. Récupérer ou créer le pays
        String countryName = dto.getCountryRegion();
        Optional<Country> country = countryService.getCountryByName(countryName);


        // 2. Récupérer ou créer la région (province/état) s’il y en a une
        Optional<Region> region = null;
        String provinceName = dto.getProvinceState();
        if (provinceName != null && !provinceName.isEmpty()) {
            // On cherche la région par nom et pays
            region = regionService.getRegionByName(provinceName);
        }

        // 3. Récupérer ou créer la location
        Location location;
        String locationName = (provinceName == null || provinceName.isEmpty()) ? countryName : provinceName;
        // On utilise le nom de la province si elle existe, sinon le nom du pays comme nom de lieu.
        if (region != null) {
            // Si une région est définie, on cherche la location par nom + région
            location = locationService.getLocationByName(locationName);

        } else {
            // Pas de région (par exemple Afghanistan n'a pas de province dans les données)
            location = locationService.getLocationByName(locationName);
        }

        // 4. Récupérer ou créer la maladie (COVID-19). On s'attend à ce qu'elle existe déjà idéalement.
        Disease disease = diseaseService.getDiseaseByName("COVID-19");

        // 5. Créer le DiseaseCase et remplir les données
        DiseaseCase diseaseCase = new DiseaseCase();
        diseaseCase.setDate(dto.getDate());
        diseaseCase.setConfirmedCases(dto.getConfirmed());
        diseaseCase.setDeaths(dto.getDeaths());
        diseaseCase.setRecovered(dto.getRecovered());
        //diseaseCase.setActive(dto.getActive());
        diseaseCase.setLocation(location);
        diseaseCase.setDisease(disease);

        return diseaseCase;
    }
}