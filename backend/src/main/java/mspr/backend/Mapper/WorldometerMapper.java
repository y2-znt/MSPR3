package mspr.backend.Mapper;


import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mspr.backend.BO.*;
import mspr.backend.Helpers.CacheHelper;
import mspr.backend.Helpers.CleanerHelper;
import mspr.backend.DTO.WorldometerDto;
import mspr.backend.Repository.CountryRepository;
import mspr.backend.Repository.LocationRepository;
import mspr.backend.Repository.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class WorldometerMapper {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private LocationRepository locationRepository;


    @Autowired
    private CacheHelper cacheHelper;

    @Autowired
    private CleanerHelper cleanerHelper;


    @PersistenceContext
    private EntityManager entityManager;

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
            cacheHelper.addCountryToCache(country.getName(), country);

            Region region = new Region();
            region.setName(country.getName()+", region standard");
            region.setCountry(country);
            regions.add(region);
            cacheHelper.addRegionToCache(region.getName(), region);

            Location location = new Location();
            location.setName(region.getName()+", location standard");
            location.setRegion(region);
            locations.add(location);
            cacheHelper.addLocationToCache(location.getName(), location);
        }


        flushEntitiesByBatch(countries, 30);
        flushEntitiesByBatch(regions, 30);
        flushEntitiesByBatch(locations, 30);

    }

    public <T> void flushEntitiesByBatch(Iterable<T> entities , int batchSize){
        int count = 0;
        for (T entity : entities) {
            entityManager.persist(entity);
            if (++count % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

}

