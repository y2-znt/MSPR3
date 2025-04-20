package mspr.backend.etl.mapper;

import mspr.backend.entity.*;
import mspr.backend.etl.helpers.*;
import mspr.backend.etl.helpers.cache.CacheManager;
import mspr.backend.etl.dto.WorldometerDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class WorldometerMapper {

    private final CacheManager cacheManager;
    private final CleanerHelper cleanerHelper;

    @Autowired
    public WorldometerMapper(CacheManager cacheManager, CleanerHelper cleanerHelper) {
        this.cacheManager = cacheManager;
        this.cleanerHelper = cleanerHelper;
    }

    /**
     * Container for the result (Country, Region, Location) returned by the Worldometer mapper.
     */
    public static class CountryRegionLocation {
        private final Country country;
        private final Region region;
        private final Location location;
        public CountryRegionLocation(Country country, Region region, Location location) {
            this.country = country;
            this.region = region;
            this.location = location;
        }
        public Country getCountry() { return country; }
        public Region getRegion() { return region; }
        public Location getLocation() { return location; }
    }

    /**
     * Converts a WorldometerDto to a Country/Region/Location triple.
     * No database saving is performed here.
     * 
     * @param dto The DTO to convert
     * @return The mapped entity triple or null if the country is in the skip list
     */
    public CountryRegionLocation toEntity(WorldometerDto dto) {
        // Vérifier si le nom du pays est dans la liste à ignorer
        if (cleanerHelper.isInSkipList(dto.getCountry())) {
            return null; // Ignorer ce DTO
        }
        
        // Create country, region, and location entities
        Country country = createCountry(dto);
        Region region = createRegion(country);
        Location location = createLocation(region);
        
        // Update country attributes
        updateCountryAttributes(country, dto);
        
        return new CountryRegionLocation(country, region, location);
    }
    
    /**
     * Creates a Country entity from the DTO
     * 
     * @param dto The DTO containing country data
     * @return The Country entity
     */
    private Country createCountry(WorldometerDto dto) {
        String countryName = cleanerHelper.cleanCountryName(dto.getCountry());
        return cacheManager.getOrCreateCountry(countryName, dto.getContinent(), dto.getWhoRegion());
    }
    
    /**
     * Creates a Region entity for the given Country
     * 
     * @param country The Country entity
     * @return The Region entity
     */
    private Region createRegion(Country country) {
        // Utilisez la méthode qui gère les cas spéciaux pour les régions standard
        return cacheManager.getOrCreateRegionWithEmptyHandling(country, null);
    }
    
    /**
     * Creates a Location entity for the given Region
     * 
     * @param region The Region entity
     * @return The Location entity
     */
    private Location createLocation(Region region) {
        // Utilisez la méthode qui gère les cas spéciaux pour les locations standard
        return cacheManager.getOrCreateLocationWithEmptyHandling(region, null);
    }
    
    /**
     * Updates attributes of the Country entity from the DTO
     * 
     * @param country The Country entity to update
     * @param dto The DTO containing country data
     */
    private void updateCountryAttributes(Country country, WorldometerDto dto) {
        country.setPopulation(dto.getPopulation());
        
        // Note : La mise à jour du continent et de la région WHO est désormais gérée par getOrCreateCountry
    }
}
