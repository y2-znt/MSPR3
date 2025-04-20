package mspr.backend.etl.helpers.cache;

import mspr.backend.entity.Country;
import mspr.backend.entity.Region;
import org.springframework.stereotype.Component;

/**
 * Cache for Region entities.
 * Uses composite key "CountryName|RegionName" as the cache key.
 */
@Component
public class RegionCache extends AbstractEntityCache<String, Region> {

    // Constants for standard names
    protected static final String STANDARD_REGION_SUFFIX = "region standard";
    protected static final String STANDARD = "standard";

    /**
     * Gets or creates a Region entity for a country and region name.
     *
     * @param country The country the region belongs to
     * @param regionName The name of the region
     * @return The Region entity, or null if country or regionName is null/empty
     */
    public Region getOrCreate(Country country, String regionName) {
        if (country == null || regionName == null || regionName.isEmpty()) {
            return null;
        }
        
        // If the region name is just "standard", replace it with country name + "region standard"
        if (STANDARD.equalsIgnoreCase(regionName)) {
            regionName = country.getName() + " - " + STANDARD_REGION_SUFFIX;
        }
        
        // Composite key for the region: "CountryName|RegionName"
        String regionKey = createKey(country, regionName);
        Region region = get(regionKey);
        
        if (region == null) {
            region = new Region();
            region.setName(regionName);
            region.setCountry(country);
            put(regionKey, region);
            logger.debug("Created new region: {} for country: {}", regionName, country.getName());
        }
        
        return region;
    }

    /**
     * Gets or creates a Region entity, handling empty region names.
     * If regionName is empty or "standard", uses country name + standard suffix.
     *
     * @param country The country the region belongs to
     * @param regionName The name of the region (can be empty or "standard")
     * @return The Region entity, or null if country is null
     */
    public Region getOrCreateWithEmptyHandling(Country country, String regionName) {
        if (country == null) {
            return null;
        }
        
        // If region name is empty or standard, use country name + standard suffix
        if (regionName == null || regionName.isEmpty() || STANDARD.equalsIgnoreCase(regionName)) {
            regionName = country.getName() + " - " + STANDARD_REGION_SUFFIX;
        }
        
        return getOrCreate(country, regionName);
    }

    /**
     * Creates the composite key for a region.
     *
     * @param country The country
     * @param regionName The region name
     * @return The composite key "CountryName|RegionName"
     */
    private String createKey(Country country, String regionName) {
        return country.getName() + "|" + regionName;
    }

    /**
     * Updates the region cache from an iterable of saved/managed Region entities.
     *
     * @param savedRegions An iterable of Region entities, typically from saveAll
     */
    public void updateCache(Iterable<Region> savedRegions) {
        clear();
        if (savedRegions != null) {
            for (Region region : savedRegions) {
                // Ensure necessary fields for the key are present
                if (region != null && region.getName() != null && 
                    region.getCountry() != null && region.getCountry().getName() != null) {
                    
                    String regionKey = createKey(region.getCountry(), region.getName());
                    put(regionKey, region);
                }
            }
            logger.debug("Updated region cache with {} entities", size());
        }
    }
} 