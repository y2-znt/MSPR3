package mspr.backend.etl.helpers;

import mspr.backend.entity.Country;
import mspr.backend.entity.Disease;
import mspr.backend.entity.Location;
import mspr.backend.entity.Region;
import mspr.backend.repository.*;
import mspr.backend.etl.exceptions.PersistenceException;
import mspr.backend.etl.helpers.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class PersistenceHelper {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceHelper.class);

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private DiseaseRepository diseaseRepository;
    @Autowired
    private CacheManager cacheManager;

    /**
     * Persists all entities currently held in the CacheManager's maps.
     * Updates the cache with the managed entities returned by the repository.
     *
     * @throws PersistenceException if there is a database error during saving.
     */
    @Transactional
    public void persistCachedEntities() throws PersistenceException {
        logger.debug("Persisting cached entities...");
        try {
            saveCountries();
            saveRegions();
            saveLocations();
            saveDiseases();
            logger.debug("Finished persisting cached entities.");
        } catch (DataAccessException e) {
            logger.error("Database error while persisting cached entities: {}", e.getMessage(), e);
            throw new PersistenceException("Error persisting cached entities to database", e);
        }
    }

    /**
     * Saves countries from the cache and updates the cache with managed entities.
     */
    private void saveCountries() {
        Map<String, Country> countriesToSave = cacheManager.getCountries();
        if (!countriesToSave.isEmpty()) {
            logger.debug("Saving {} countries", countriesToSave.size());
            List<Country> savedCountries = countryRepository.saveAll(countriesToSave.values());
            cacheManager.setCountries(savedCountries); // Update cache with managed entities
            logger.debug("Updated country cache with {} managed entities", savedCountries.size());
        } else {
            logger.debug("No new countries to save.");
        }
    }

    /**
     * Saves regions from the cache and updates the cache with managed entities.
     */
    private void saveRegions() {
        Map<String, Region> regionsToSave = cacheManager.getRegions();
        if (!regionsToSave.isEmpty()) {
            logger.debug("Saving {} regions", regionsToSave.size());
            List<Region> savedRegions = regionRepository.saveAll(regionsToSave.values());
            cacheManager.setRegions(savedRegions); // Update cache with managed entities
            logger.debug("Updated region cache with {} managed entities", savedRegions.size());
        } else {
            logger.debug("No new regions to save.");
        }
    }

    /**
     * Saves locations from the cache and updates the cache with managed entities.
     */
    private void saveLocations() {
        Map<String, Location> locationsToSave = cacheManager.getLocations();
        if (!locationsToSave.isEmpty()) {
            logger.debug("Saving {} locations", locationsToSave.size());
            List<Location> savedLocations = locationRepository.saveAll(locationsToSave.values());
            cacheManager.setLocations(savedLocations); // Update cache with managed entities
            logger.debug("Updated location cache with {} managed entities", savedLocations.size());
        } else {
            logger.debug("No new locations to save.");
        }
    }

    /**
     * Saves diseases from the cache and updates the cache with managed entities.
     */
    private void saveDiseases() {
        Map<String, Disease> diseasesToSave = cacheManager.getDiseases();
        if (!diseasesToSave.isEmpty()) {
            logger.debug("Saving {} diseases", diseasesToSave.size());
            List<Disease> savedDiseases = diseaseRepository.saveAll(diseasesToSave.values());
            cacheManager.setDiseases(savedDiseases); // Update cache with managed entities
            logger.debug("Updated disease cache with {} managed entities", savedDiseases.size());
        } else {
            logger.debug("No new diseases to save.");
        }
    }
} 