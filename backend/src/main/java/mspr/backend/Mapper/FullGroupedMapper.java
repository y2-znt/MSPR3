package mspr.backend.Mapper;

import mspr.backend.BO.*;
import mspr.backend.DTO.FullGroupedDto;
import mspr.backend.DTO.UsaCountyDto;
import mspr.backend.Service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FullGroupedMapper {

    @Autowired
    private CountryService countryService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private DiseaseService diseaseService;
    @Autowired
    private RegionService regionService;

    /**
     * Convertit un DTO FullGroupedDto en une entité DiseaseCase.
     * Le DTO ne contient qu'une agrégation par pays.
     * On crée (si nécessaire) le Country et une Location correspondant au pays.
     */
    public DiseaseCase dtoToEntity(FullGroupedDto dto) {
        // 1. Récupérer ou créer le pays à partir du nom fourni dans le DTO.
        Optional<Country> country = countryService.getCountryByName(dto.getCountryRegion());


        // TODO set region before location
//        Region region = regionService

        // 2. Pour le fichier FullGrouped, nous n'avons pas de province, donc
        // on utilise le nom du pays comme nom de Location.
        String locationName = dto.getCountryRegion();
        Location location = locationService.getLocationByName(locationName);

        // 3. Récupérer ou créer la maladie "COVID-19"
        Disease disease = diseaseService.getDiseaseByName("COVID-19");

        // 4. Créer et remplir le DiseaseCase avec les informations du DTO.
        DiseaseCase diseaseCase = new DiseaseCase();
        diseaseCase.setDate(dto.getDate());
        diseaseCase.setConfirmedCases(dto.getConfirmed());
        diseaseCase.setDeaths(dto.getDeaths());
        diseaseCase.setRecovered(dto.getRecovered());
//        diseaseCase.setActive(dto.getActive());
        diseaseCase.setLocation(location);;

        diseaseCase.setDisease(disease);

        return diseaseCase;
    }

}
