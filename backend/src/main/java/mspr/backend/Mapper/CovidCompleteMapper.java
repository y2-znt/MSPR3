package mspr.backend.Mapper;

import mspr.backend.DTO.CovidCompleteDto;
import mspr.backend.BO.*;
import mspr.backend.Repository.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class CovidCompleteMapper {

    @Autowired private CountryRepository countryRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private DiseaseRepository diseaseRepository;

    /**
     * Convertit un DTO CovidCompleteDto en un objet DiseaseCase (prêt à être persisté).
     * Cette méthode gère la création/recherche de Country, Region, Location, et lie la Disease (COVID-19).
     */
    public DiseaseCase dtoToEntity(CovidCompleteDto dto) {
        // 1. Récupérer ou créer le pays
        String countryName = dto.getCountryRegion();
        Country country = countryRepository.findByName(countryName)
                .orElseGet(() -> {
                    // Si le pays n'existe pas encore, on le crée
                    Country newCountry = new Country();
                    newCountry.setName(countryName);
                    // On peut éventuellement renseigner continent/population si connu via d'autres sources plus tard
                    return countryRepository.save(newCountry);
                });

        // 2. Récupérer ou créer la région (province/état) s’il y en a une
        Region region = null;
        String provinceName = dto.getProvinceState();
        if (provinceName != null && !provinceName.isEmpty()) {
            // On cherche la région par nom et pays
            region = regionRepository.findByNameAndCountryId(provinceName, country.getId())
                    .orElseGet(() -> {
                        Region newRegion = new Region();
                        newRegion.setName(provinceName);
                        newRegion.setCountry(country);
                        return regionRepository.save(newRegion);
                    });
        }

        // 3. Récupérer ou créer la location
        Location location;
        String locationName = (provinceName == null || provinceName.isEmpty()) ? countryName : provinceName;
        // On utilise le nom de la province si elle existe, sinon le nom du pays comme nom de lieu.
        if (region != null) {
            // Si une région est définie, on cherche la location par nom + région
            location = locationRepository.findByNameAndRegionId(locationName, region.getId())
                    .orElseGet(() -> {
                        Location newLocation = new Location();
                        newLocation.setName(locationName);
//                        newLocation.setRegion(region);
                        return locationRepository.save(newLocation);
                    });
        } else {
            // Pas de région (par exemple Afghanistan n'a pas de province dans les données)
            location = locationRepository.findByNameAndCountryId(locationName, country.getId())
                    .orElseGet(() -> {
                        Location newLocation = new Location();
                        newLocation.setName(locationName);
                        // region reste null
                        return locationRepository.save(newLocation);
                    });
        }

        // 4. Récupérer ou créer la maladie (COVID-19). On s'attend à ce qu'elle existe déjà idéalement.
        Disease disease = diseaseRepository.findByName("COVID-19")
                .orElseGet(() -> {
                    Disease newDisease = new Disease();
                    newDisease.setName("COVID-19");
                    return diseaseRepository.save(newDisease);
                });

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