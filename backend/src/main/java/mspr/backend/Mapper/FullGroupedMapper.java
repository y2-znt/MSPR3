package mspr.backend.Mapper;

import mspr.backend.BO.*;
import mspr.backend.DTO.FullGroupedDto;
import mspr.backend.DTO.UsaCountyDto;
import mspr.backend.Repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FullGroupedMapper {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DiseaseRepository diseaseRepository;
    @Autowired
    private RegionRepository regionRepository;

    /**
     * Convertit un DTO FullGroupedDto en une entité DiseaseCase.
     * Le DTO ne contient qu'une agrégation par pays.
     * On crée (si nécessaire) le Country et une Location correspondant au pays.
     */
    public DiseaseCase dtoToEntity(FullGroupedDto dto) {
        // 1. Récupérer ou créer le pays à partir du nom fourni dans le DTO.
        Country country = countryRepository.findByName(dto.getCountryRegion())
                .orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setName(dto.getCountryRegion());
                    return countryRepository.save(newCountry);
                });


        // TODO set region before location
//        Region region = regionRepository

        // 2. Pour le fichier FullGrouped, nous n'avons pas de province, donc
        // on utilise le nom du pays comme nom de Location.
        String locationName = dto.getCountryRegion();
        Location location = locationRepository.findByNameAndCountryId(locationName, country.getId())
                .orElseGet(() -> {
                    Location newLocation = new Location();
                    newLocation.setName(locationName);
                    // Les coordonnées (lat, lon) ne sont pas disponibles dans ce CSV.

//                    newLocation.setRegion(region);
                    return locationRepository.save(newLocation);
                });

        // 3. Récupérer ou créer la maladie "COVID-19"
        Disease disease = diseaseRepository.findByName("COVID-19")
                .orElseGet(() -> {
                    Disease newDisease = new Disease();
                    newDisease.setName("COVID-19");
                    return diseaseRepository.save(newDisease);
                });

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
