package mspr.backend.ETL.Mapper;

import mspr.backend.BO.*;
import mspr.backend.ETL.Helpers.*;
import mspr.backend.ETL.DTO.WorldometerDto;
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
        // Clean country name
        String countryName = cleanerHelper.cleanCountryName(dto.getCountry());
        Country country = cacheHelper.getOrCreateCountry(countryName);
        Region region = cacheHelper.getOrCreateRegion(country, STANDARD_REGION_NAME);
        Location location = cacheHelper.getOrCreateLocation(region, STANDARD_LOCATION_NAME);
        
        // Update country static attributes (e.g., population)
        country.setPopulation(dto.getPopulation());
        // (Other dto fields, like totalCases, can be used if needed)
        
        return new CountryRegionLocation(country, region, location);
    }
}
