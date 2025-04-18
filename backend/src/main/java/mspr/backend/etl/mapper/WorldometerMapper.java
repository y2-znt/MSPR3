package mspr.backend.etl.mapper;

import mspr.backend.BO.*;
import mspr.backend.etl.helpers.*;
import mspr.backend.etl.dto.WorldometerDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class WorldometerMapper {

    // Constants for standard names
    public static final String STANDARD_REGION_NAME = "standard";
    public static final String STANDARD_LOCATION_NAME = "standard";

    private final CacheHelper cacheHelper;
    private final CleanerHelper cleanerHelper;

    @Autowired
    public WorldometerMapper(CacheHelper cacheHelper, CleanerHelper cleanerHelper) {
        this.cacheHelper = cacheHelper;
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
     */
    public CountryRegionLocation toEntity(WorldometerDto dto) {
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
        return cacheHelper.getOrCreateCountry(countryName);
    }
    
    /**
     * Creates a Region entity for the given Country
     * 
     * @param country The Country entity
     * @return The Region entity
     */
    private Region createRegion(Country country) {
        return cacheHelper.getOrCreateRegion(country, STANDARD_REGION_NAME);
    }
    
    /**
     * Creates a Location entity for the given Region
     * 
     * @param region The Region entity
     * @return The Location entity
     */
    private Location createLocation(Region region) {
        return cacheHelper.getOrCreateLocation(region, STANDARD_LOCATION_NAME);
    }
    
    /**
     * Updates attributes of the Country entity from the DTO
     * 
     * @param country The Country entity to update
     * @param dto The DTO containing country data
     */
    private void updateCountryAttributes(Country country, WorldometerDto dto) {
        country.setPopulation(dto.getPopulation());
        // Other attributes can be updated here if needed
    }
}
