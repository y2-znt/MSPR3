package mspr.backend.Mapper;


import mspr.backend.BO.*;
import mspr.backend.DTO.WorldometerDto;
import mspr.backend.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorldometerMapper {
    @Autowired
    private CountryService countryService;
    public Country dtoToEntity(WorldometerDto dto) {
        Optional<Country> country = countryService.getCountryByName(dto.getCountry());
//        country.setName(dto.getCountry());

//        country.setContinent(Country.ContinentEnum.valueOf(dto.getContinent().toUpperCase()));
//        country.setWhoRegion(Country.WHORegionEnum.valueOf(dto.getWhoRegion().replace(" ", "_")));
//        country.setPopulation(dto.getPopulation());
        // On met peut-être à jour d'autres infos si besoin (totalCases etc. pourraient être utilisés ailleurs)
        //return countryService.save(country);
        return new Country();
    }
}

