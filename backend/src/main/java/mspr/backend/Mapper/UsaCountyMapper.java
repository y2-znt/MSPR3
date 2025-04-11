package mspr.backend.Mapper;

import mspr.backend.DTO.UsaCountyDto;
import mspr.backend.BO.*;
import mspr.backend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class UsaCountyMapper {
    @Autowired private CountryRepository countryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private DiseaseRepository diseaseRepository;

    public DiseaseCase dtoToEntity(UsaCountyDto dto) {
        // Pays (devrait toujours être "US")
        Country country = countryRepository.findByName(dto.getCountryRegion())
                .orElseGet(() -> {
                    Country c = new Country();
                    c.setName(dto.getCountryRegion());
                    return countryRepository.save(c);
                });

        // Région (l'État aux USA)
        Region state = regionRepository.findByNameAndCountryId(dto.getProvinceState(), country.getId())
                .orElseGet(() -> {
                    Region r = new Region();
                    r.setName(dto.getProvinceState());
                    r.setCountry(country);
                    return regionRepository.save(r);
                });

        // Location (le comté, rattaché à l'État)
        Location location = locationRepository.findByNameAndRegionId(dto.getCounty(), state.getId())
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName(dto.getCounty());
                    loc.setRegion(state);
                    return locationRepository.save(loc);
                });

        // Maladie (COVID-19)
        Disease disease = diseaseRepository.findByName("COVID-19")
                .orElseGet(() -> diseaseRepository.save(new Disease("COVID-19")));

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

