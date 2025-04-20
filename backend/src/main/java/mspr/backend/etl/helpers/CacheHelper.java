package mspr.backend.etl.helpers;

import mspr.backend.entity.*;
import mspr.backend.repository.*;
import mspr.backend.etl.helpers.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Legacy CacheHelper that delegates to the new CacheManager.
 * Maintains backward compatibility with existing code.
 * 
 * @deprecated Use {@link mspr.backend.etl.helpers.cache.CacheManager} directly instead.
 */
@Component
@Deprecated
public class CacheHelper {
    private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DiseaseRepository diseaseRepository;

    @Autowired
    private CleanerHelper cleanerHelper;

    // Constants for standard names - kept for backward compatibility
    private static final String STANDARD_REGION_SUFFIX = "region standard";
    private static final String STANDARD_LOCATION_SUFFIX = "location standard";
    private static final String STANDARD = "standard";

    /**
     * Gets or creates a Country entity with just a name.
     * Delegates to CacheManager.
     * 
     * @param countryName The name of the country
     * @return The Country entity
     */
    public Country getOrCreateCountry(String countryName) {
        return cacheManager.getOrCreateCountry(countryName);
    }

    /**
     * Gets or creates a Country entity with name, continent and WHO region.
     * Delegates to CacheManager.
     * 
     * @param countryName The name of the country
     * @param continentStr The continent name (will be cleaned)
     * @param whoRegionStr The WHO region name (will be cleaned)
     * @return The Country entity
     */
    public Country getOrCreateCountry(String countryName, String continentStr, String whoRegionStr) {
        return cacheManager.getOrCreateCountry(countryName, continentStr, whoRegionStr);
    }

    /**
     * Gets or creates a Region entity.
     * Delegates to CacheManager.
     * 
     * @param country The country the region belongs to
     * @param regionName The name of the region
     * @return The Region entity
     */
    public Region getOrCreateRegion(Country country, String regionName) {
        return cacheManager.getOrCreateRegion(country, regionName);
    }

    /**
     * Gets or creates a Location entity.
     * Delegates to CacheManager.
     * 
     * @param region The region the location belongs to
     * @param locationName The name of the location
     * @return The Location entity
     */
    public Location getOrCreateLocation(Region region, String locationName) {
        return cacheManager.getOrCreateLocation(region, locationName);
    }

    /**
     * Gets or creates a Region entity, handling empty region names.
     * Delegates to CacheManager.
     * 
     * @param country The country the region belongs to
     * @param regionName The name of the region (can be empty or "standard")
     * @return The Region entity
     */
    public Region getOrCreateRegionWithEmptyHandling(Country country, String regionName) {
        return cacheManager.getOrCreateRegionWithEmptyHandling(country, regionName);
    }

    /**
     * Gets or creates a Location entity, handling empty location names.
     * Delegates to CacheManager.
     * 
     * @param region The region the location belongs to
     * @param locationName The name of the location (can be empty or "standard")
     * @return The Location entity
     */
    public Location getOrCreateLocationWithEmptyHandling(Region region, String locationName) {
        return cacheManager.getOrCreateLocationWithEmptyHandling(region, locationName);
    }

    /**
     * Gets the location key for a country, region, and location name.
     * Delegates to CacheManager.
     * 
     * @param countryName The name of the country
     * @param regionName The name of the region
     * @param locationName The name of the location
     * @return The location key
     */
    public String getLocationKey(String countryName, String regionName, String locationName) {
        return cacheManager.getLocationKey(countryName, regionName, locationName);
    }

    /**
     * Gets or creates a Disease entity.
     * Delegates to CacheManager.
     * 
     * @param diseaseName The name of the disease
     * @return The Disease entity
     */
    public Disease getOrCreateDisease(String diseaseName) {
        return cacheManager.getOrCreateDisease(diseaseName);
    }

    /**
     * Adds a Disease entity to the cache.
     * Delegates to CacheManager.
     * 
     * @param diseaseName The name of the disease
     * @param disease The Disease entity
     */
    public void addDiseaseToCache(String diseaseName, Disease disease) {
        cacheManager.addDiseaseToCache(diseaseName, disease);
    }

    /**
     * Gets all countries from the cache.
     * Delegates to CacheManager.
     * 
     * @return Map of country names to Country entities
     */
    public Map<String, Country> getCountries() {
        return cacheManager.getCountries();
    }

    /**
     * Gets all regions from the cache.
     * Delegates to CacheManager.
     * 
     * @return Map of region keys to Region entities
     */
    public Map<String, Region> getRegions() {
        return cacheManager.getRegions();
    }

    /**
     * Gets all locations from the cache.
     * Delegates to CacheManager.
     * 
     * @return Map of location keys to Location entities
     */
    public Map<String, Location> getLocations() {
        return cacheManager.getLocations();
    }

    /**
     * Gets all diseases from the cache.
     * Delegates to CacheManager.
     * 
     * @return Map of disease names to Disease entities
     */
    public Map<String, Disease> getDiseases() {
        return cacheManager.getDiseases();
    }

    /**
     * Updates the country cache from an iterable of saved/managed Country entities.
     * Delegates to CacheManager.
     * 
     * @param savedCountries An iterable of Country entities
     */
    public void setCountries(Iterable<Country> savedCountries) {
        cacheManager.setCountries(savedCountries);
    }

    /**
     * Updates the region cache from an iterable of saved/managed Region entities.
     * Delegates to CacheManager.
     * 
     * @param savedRegions An iterable of Region entities
     */
    public void setRegions(Iterable<Region> savedRegions) {
        cacheManager.setRegions(savedRegions);
    }

    /**
     * Updates the location cache from an iterable of saved/managed Location entities.
     * Delegates to CacheManager.
     * 
     * @param savedLocations An iterable of Location entities
     */
    public void setLocations(Iterable<Location> savedLocations) {
        cacheManager.setLocations(savedLocations);
    }

    /**
     * Updates the disease cache from an iterable of saved/managed Disease entities.
     * Delegates to CacheManager.
     * 
     * @param savedDiseases An iterable of Disease entities
     */
    public void setDiseases(Iterable<Disease> savedDiseases) {
        cacheManager.setDiseases(savedDiseases);
    }
}
