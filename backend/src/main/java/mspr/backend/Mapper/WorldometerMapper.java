package mspr.backend.Mapper;


import mspr.backend.BO.*;
import mspr.backend.Cleaner.CleanerHelper;
import mspr.backend.DTO.WorldometerDto;
import mspr.backend.Repository.CountryRepository;
import mspr.backend.Repository.LocationRepository;
import mspr.backend.Repository.RegionRepository;
import mspr.backend.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Component
public class WorldometerMapper {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CleanerHelper cleanerHelper;

    public void dtoToEntity(HashMap<Integer, WorldometerDto> dtoMap) {

        ArrayList<Country> countries = new ArrayList<>();
        ArrayList<Region> regions = new ArrayList<>();
        ArrayList<Location> locations = new ArrayList<>();

        for (WorldometerDto worldometerDto : dtoMap.values()) {
            Country country = new Country();
            country.setName(cleanerHelper.cleanCountryName(worldometerDto.getCountry()));
            country.setContinent(cleanerHelper.cleanContinent(worldometerDto.getContinent()));
            country.setWhoRegion(cleanerHelper.cleanWhoRegion(worldometerDto.getWhoRegion()));
            country.setPopulation(worldometerDto.getPopulation());

            countries.add(country);

            Region region = new Region();
            region.setName(country.getName()+", region standard");
            region.setCountry(country);
            regions.add(region);

            Location location = new Location();
            location.setName(region.getName()+", location standard");
            location.setRegion(region);
            locations.add(location);
        }

        countryRepository.saveAll(countries);
        regionRepository.saveAll(regions);
        locationRepository.saveAll(locations);
    }
}

