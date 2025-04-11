package mspr.backend.Mapper;

import mspr.backend.DTO.UsaCountyDto;
import mspr.backend.BO.*;
import mspr.backend.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class UsaCountyMapper {
    @Autowired private CountryService countryService;
    @Autowired private RegionService regionService;
    @Autowired private LocationService locationService;
    @Autowired private DiseaseService diseaseService;

    public DiseaseCase dtoToEntity(UsaCountyDto dto) {
        // Pays (devrait toujours être "US")
        Optional<Country> country = countryService.getCountryByName(dto.getCountryRegion());

        // Région (l'État aux USA)
        Optional<Region> state = regionService.getRegionByName(dto.getProvinceState());

        // Location (le comté, rattaché à l'État)
        Location location = locationService.getLocationByName(dto.getCounty());

        // Maladie (COVID-19)
        Disease disease = diseaseService.getDiseaseByName("COVID-19");

        // Création du cas
        DiseaseCase diseaseCase = new DiseaseCase();
        diseaseCase.setDate(dto.getDate());
        diseaseCase.setConfirmedCases(dto.getConfirmed());
        diseaseCase.setDeaths(dto.getDeaths());
        diseaseCase.setRecovered(dto.getRecovered()); // sera 0 dans ce contexte
//        diseaseCase.setActive(dto.getActive());       // sera 0 dans ce contexte
        diseaseCase.setLocation(location);
        diseaseCase.setDisease(disease);

        return diseaseCase;
    }
}

