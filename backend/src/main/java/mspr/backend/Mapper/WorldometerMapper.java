package mspr.backend.Mapper;


import mspr.backend.BO.*;
import mspr.backend.DTO.WorldometerDto;
import mspr.backend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorldometerMapper {
    @Autowired
    private CountryRepository countryRepository;
    public Country dtoToEntity(WorldometerDto dto) {
        Country country = countryRepository.findByName(dto.getCountry())
                .orElseGet(() -> new Country());
        country.setName(dto.getCountry());


//        country.setContinent(Country.ContinentEnum.valueOf(dto.getContinent().toUpperCase()));
//        country.setWhoRegion(Country.WHORegionEnum.valueOf(dto.getWhoRegion().replace(" ", "_")));
        country.setPopulation(dto.getPopulation());
        // On met peut-être à jour d'autres infos si besoin (totalCases etc. pourraient être utilisés ailleurs)
        return countryRepository.save(country);
    }
}

