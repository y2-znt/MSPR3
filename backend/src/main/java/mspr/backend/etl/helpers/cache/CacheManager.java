package mspr.backend.etl.helpers.cache;

import mspr.backend.BO.Country;
import mspr.backend.BO.Disease;
import mspr.backend.BO.Location;
import mspr.backend.BO.Region;
import mspr.backend.etl.helpers.CleanerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Cache manager facade that coordinates all entity caches.
 * Provides a unified interface for cache operations.
 */
@Component
public class CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    @Autowired
    private CountryCache countryCache;

    @Autowired
    private RegionCache regionCache;

    @Autowired
    private LocationCache locationCache;

    @Autowired
    private DiseaseCache diseaseCache;

    @Autowired
    private CleanerHelper cleanerHelper;

    // Constants from the original CacheHelper (for backward compatibility)
    private static final String STANDARD_REGION_SUFFIX = "region standard";
    private static final String STANDARD_LOCATION_SUFFIX = "location standard";
    private static final String STANDARD = "standard";

    /**
     * Gets or creates a Country entity with just a name.
     * @param countryName The name of the country
     * @return The Country entity
     */
    public Country getOrCreateCountry(String countryName) {
        return countryCache.getOrCreate(countryName);
    }

    /**
     * Gets or creates a Country entity with name, continent and WHO region.
     * @param countryName The name of the country
     * @param continentStr The continent name (will be cleaned)
     * @param whoRegionStr The WHO region name (will be cleaned)
     * @return The Country entity
     */
    public Country getOrCreateCountry(String countryName, String continentStr, String whoRegionStr) {
        return countryCache.getOrCreate(countryName, continentStr, whoRegionStr);
    }

    /**
     * Gets or creates a Region entity.
     * @param country The country the region belongs to
     * @param regionName The name of the region
     * @return The Region entity
     */
    public Region getOrCreateRegion(Country country, String regionName) {
        return regionCache.getOrCreate(country, regionName);
    }

    /**
     * Gets or creates a Region entity, handling empty region names.
     * @param country The country the region belongs to
     * @param regionName The name of the region (can be empty or "standard")
     * @return The Region entity
     */
    public Region getOrCreateRegionWithEmptyHandling(Country country, String regionName) {
        return regionCache.getOrCreateWithEmptyHandling(country, regionName);
    }

    /**
     * Gets or creates a Location entity.
     * @param region The region the location belongs to
     * @param locationName The name of the location
     * @return The Location entity
     */
    public Location getOrCreateLocation(Region region, String locationName) {
        return locationCache.getOrCreate(region, locationName);
    }

    /**
     * Gets or creates a Location entity, handling empty location names.
     * @param region The region the location belongs to
     * @param locationName The name of the location (can be empty or "standard")
     * @return The Location entity
     */
    public Location getOrCreateLocationWithEmptyHandling(Region region, String locationName) {
        return locationCache.getOrCreateWithEmptyHandling(region, locationName);
    }

    /**
     * Gets the location key for a country, region, and location name.
     * @param countryName The name of the country
     * @param regionName The name of the region
     * @param locationName The name of the location
     * @return The location key
     */
    public String getLocationKey(String countryName, String regionName, String locationName) {
        return locationCache.getLocationKey(countryName, regionName, locationName);
    }

    /**
     * Gets or creates a Disease entity.
     * @param diseaseName The name of the disease
     * @return The Disease entity
     */
    public Disease getOrCreateDisease(String diseaseName) {
        return diseaseCache.getOrCreate(diseaseName);
    }

    /**
     * Adds a Disease entity to the cache.
     * @param diseaseName The name of the disease
     * @param disease The Disease entity
     */
    public void addDiseaseToCache(String diseaseName, Disease disease) {
        diseaseCache.put(diseaseName, disease);
    }

    /**
     * Gets all countries from the cache.
     * @return Map of country names to Country entities
     */
    public Map<String, Country> getCountries() {
        return countryCache.getAll();
    }

    /**
     * Gets all regions from the cache.
     * @return Map of region keys to Region entities
     */
    public Map<String, Region> getRegions() {
        return regionCache.getAll();
    }

    /**
     * Gets all locations from the cache.
     * @return Map of location keys to Location entities
     */
    public Map<String, Location> getLocations() {
        return locationCache.getAll();
    }

    /**
     * Gets all diseases from the cache.
     * @return Map of disease names to Disease entities
     */
    public Map<String, Disease> getDiseases() {
        return diseaseCache.getAll();
    }

    /**
     * Updates the country cache from an iterable of saved/managed Country entities.
     * @param savedCountries An iterable of Country entities
     */
    public void setCountries(Iterable<Country> savedCountries) {
        countryCache.updateCache(savedCountries);
    }

    /**
     * Updates the region cache from an iterable of saved/managed Region entities.
     * @param savedRegions An iterable of Region entities
     */
    public void setRegions(Iterable<Region> savedRegions) {
        regionCache.updateCache(savedRegions);
    }

    /**
     * Updates the location cache from an iterable of saved/managed Location entities.
     * @param savedLocations An iterable of Location entities
     */
    public void setLocations(Iterable<Location> savedLocations) {
        locationCache.updateCache(savedLocations);
    }

    /**
     * Updates the disease cache from an iterable of saved/managed Disease entities.
     * @param savedDiseases An iterable of Disease entities
     */
    public void setDiseases(Iterable<Disease> savedDiseases) {
        diseaseCache.updateCache(savedDiseases);
    }
} 