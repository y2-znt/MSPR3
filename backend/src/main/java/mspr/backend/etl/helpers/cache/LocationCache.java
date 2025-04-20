package mspr.backend.etl.helpers.cache;

import mspr.backend.BO.Location;
import mspr.backend.BO.Region;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache for Location entities.
 * Uses composite key "CountryName|RegionName|LocationName" as the cache key.
 */
@Component
public class LocationCache extends AbstractEntityCache<String, Location> {

    // Secondary cache for detecting duplicate location names across different regions
    private final Map<String, Location> locationsByNameOnly = new HashMap<>();

    // Constants for standard names
    protected static final String STANDARD_LOCATION_SUFFIX = "location standard";
    protected static final String STANDARD = "standard";

    /**
     * Gets or creates a Location entity for a region and location name.
     *
     * @param region The region the location belongs to
     * @param locationName The name of the location
     * @return The Location entity, or null if region or locationName is null/empty
     */
    public Location getOrCreate(Region region, String locationName) {
        if (region == null || locationName == null || locationName.isEmpty()) {
            return null;
        }
        
        // If the location name is just "standard", replace it with region name + "location standard"
        if (STANDARD.equalsIgnoreCase(locationName)) {
            locationName = region.getName() + " - " + STANDARD_LOCATION_SUFFIX;
        }
        
        // Composite key for the location: "CountryName|RegionName|LocationName"
        String locationKey = createKey(region, locationName);
        Location location = get(locationKey);
        
        if (location == null) {
            // Check if an identical name exists in another region
            Location existingByName = locationsByNameOnly.get(locationName);
            if (existingByName != null && existingByName.getRegion() != region) {
                // If a location with this name already exists in a different region, 
                // modify the name to make it unique
                String uniqueName = region.getName() + " - " + locationName;
                
                // Check if this unique version already exists
                String uniqueKey = createKey(region, uniqueName);
                Location existingUnique = get(uniqueKey);
                
                if (existingUnique != null) {
                    return existingUnique;
                }
                
                location = new Location();
                location.setName(uniqueName);
                location.setRegion(region);
                put(uniqueKey, location);
                locationsByNameOnly.put(uniqueName, location);
                logger.debug("Created new location with unique name: {} for region: {}", 
                            uniqueName, region.getName());
            } else {
                // Original name is unique or in the same region, we can use it as is
                location = new Location();
                location.setName(locationName);
                location.setRegion(region);
                put(locationKey, location);
                locationsByNameOnly.put(locationName, location);
                logger.debug("Created new location: {} for region: {}", locationName, region.getName());
            }
        }
        
        return location;
    }

    /**
     * Gets or creates a Location entity, handling empty location names.
     * If locationName is empty or "standard", uses region name + standard suffix.
     *
     * @param region The region the location belongs to
     * @param locationName The name of the location (can be empty or "standard")
     * @return The Location entity, or null if region is null
     */
    public Location getOrCreateWithEmptyHandling(Region region, String locationName) {
        if (region == null) {
            return null;
        }
        
        // If location name is empty or standard, use region name + standard suffix
        if (locationName == null || locationName.isEmpty() || STANDARD.equalsIgnoreCase(locationName)) {
            locationName = region.getName() + " - " + STANDARD_LOCATION_SUFFIX;
        }
        
        return getOrCreate(region, locationName);
    }

    /**
     * Creates the composite key for a location.
     *
     * @param region The region
     * @param locationName The location name
     * @return The composite key "CountryName|RegionName|LocationName"
     */
    private String createKey(Region region, String locationName) {
        return region.getCountry().getName() + "|" + region.getName() + "|" + locationName;
    }

    /**
     * Generates the standard cache key for a location.
     *
     * @param countryName The name of the country
     * @param regionName The name of the region
     * @param locationName The name of the location
     * @return The composite key used for the location cache, or null if any part is null/empty
     */
    public String getLocationKey(String countryName, String regionName, String locationName) {
        if (countryName == null || countryName.isEmpty() ||
            regionName == null || regionName.isEmpty() ||
            locationName == null || locationName.isEmpty()) {
            return null;
        }
        return countryName + "|" + regionName + "|" + locationName;
    }

    /**
     * Updates the location cache from an iterable of saved/managed Location entities.
     *
     * @param savedLocations An iterable of Location entities, typically from saveAll
     */
    public void updateCache(Iterable<Location> savedLocations) {
        clear();
        this.locationsByNameOnly.clear();
        
        if (savedLocations != null) {
            for (Location location : savedLocations) {
                // Ensure necessary fields for the key are present
                if (location != null && location.getName() != null &&
                    location.getRegion() != null && location.getRegion().getName() != null &&
                    location.getRegion().getCountry() != null && location.getRegion().getCountry().getName() != null)
                {
                    String locationKey = createKey(location.getRegion(), location.getName());
                    put(locationKey, location);
                    this.locationsByNameOnly.put(location.getName(), location);
                }
            }
            logger.debug("Updated location cache with {} entities", size());
        }
    }

    /**
     * Clears both the main cache and the by-name-only cache.
     */
    @Override
    public void clear() {
        super.clear();
        this.locationsByNameOnly.clear();
    }
} 