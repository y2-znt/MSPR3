package mspr.backend.etl.helpers.cache;

import mspr.backend.BO.Country;
import mspr.backend.etl.helpers.CleanerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cache for Country entities.
 * Uses country name as the key.
 */
@Component
public class CountryCache extends AbstractEntityCache<String, Country> {

    @Autowired
    private CleanerHelper cleanerHelper;

    // Constants for standard names
    protected static final String STANDARD = "standard";

    /**
     * Gets or creates a Country entity with just a name.
     *
     * @param countryName The name of the country
     * @return The Country entity, or null if countryName is null/empty
     */
    public Country getOrCreate(String countryName) {
        return getOrCreate(countryName, null, null);
    }

    /**
     * Gets or creates a Country entity with name, continent and WHO region.
     *
     * @param countryName The name of the country
     * @param continentStr The continent name (will be cleaned)
     * @param whoRegionStr The WHO region name (will be cleaned)
     * @return The Country entity, or null if countryName is null/empty
     */
    public Country getOrCreate(String countryName, String continentStr, String whoRegionStr) {
        if (countryName == null || countryName.isEmpty()) {
            logger.debug("Null or empty country name, returning null");
            return null;
        }
        
        Country country = get(countryName);
        
        if (country == null) {
            country = new Country();
            country.setName(countryName);
            
            // Check for special case continent first
            Country.ContinentEnum specialContinent = cleanerHelper.getSpecialCaseContinent(countryName);
            if (specialContinent != null) {
                country.setContinent(specialContinent);
                logger.debug("Set special case continent {} for country {}", specialContinent, countryName);
            } else {
                // Clean and set continent if provided
                Country.ContinentEnum continent = parseContinent(continentStr);
                if (continent != null) {
                    country.setContinent(continent);
                    logger.debug("Set continent {} for country {}", continent, countryName);
                } else {
                    logger.debug("No valid continent found for string: '{}'", continentStr);
                }
            }
            
            // Check for special case WHO region first
            Country.WHORegionEnum specialWhoRegion = cleanerHelper.getSpecialCaseWhoRegion(countryName);
            if (specialWhoRegion != null) {
                country.setWhoRegion(specialWhoRegion);
                logger.debug("Set special case WHO region {} for country {}", specialWhoRegion, countryName);
            } else {
                // Clean and set WHO region if provided
                Country.WHORegionEnum whoRegion = parseWhoRegion(whoRegionStr);
                if (whoRegion != null) {
                    country.setWhoRegion(whoRegion);
                    logger.debug("Set WHO region {} for country {}", whoRegion, countryName);
                } else {
                    logger.debug("No valid WHO region found for string: '{}'", whoRegionStr);
                }
            }
            
            // If WHO region is null but continent is defined, deduce WHO region
            if (country.getWhoRegion() == null && country.getContinent() != null) {
                Country.WHORegionEnum whoRegion = deduceWhoRegionFromContinent(country.getContinent());
                if (whoRegion != null) {
                    country.setWhoRegion(whoRegion);
                    logger.debug("Deduced WHO region {} from continent {} for country {}", 
                                whoRegion, country.getContinent(), countryName);
                }
            }
            
            put(countryName, country);
            logger.debug("Created new country: {} with continent: {} and WHO region: {}", 
                        countryName, country.getContinent(), country.getWhoRegion());
        } else {
            // Check for special cases for existing countries that might be missing data
            updateExistingCountry(country);
        }
        
        return country;
    }
    
    /**
     * Updates an existing country with any missing data.
     *
     * @param country The country to update
     */
    private void updateExistingCountry(Country country) {
        if (country == null) return;
        
        String countryName = country.getName();
        
        // Update continent if missing
        if (country.getContinent() == null) {
            Country.ContinentEnum specialContinent = cleanerHelper.getSpecialCaseContinent(countryName);
            if (specialContinent != null) {
                country.setContinent(specialContinent);
                logger.debug("Updated existing country {} with special case continent {}", 
                           countryName, specialContinent);
            }
        }
        
        // Update WHO region if missing
        if (country.getWhoRegion() == null) {
            Country.WHORegionEnum specialWhoRegion = cleanerHelper.getSpecialCaseWhoRegion(countryName);
            if (specialWhoRegion != null) {
                country.setWhoRegion(specialWhoRegion);
                logger.debug("Updated existing country {} with special case WHO region {}", 
                           countryName, specialWhoRegion);
            } else if (country.getContinent() != null) {
                // If WHO region is null, try to deduce it from continent
                Country.WHORegionEnum whoRegion = deduceWhoRegionFromContinent(country.getContinent());
                if (whoRegion != null) {
                    country.setWhoRegion(whoRegion);
                    logger.debug("Updated existing country {} with deduced WHO region {} from continent {}", 
                               countryName, whoRegion, country.getContinent());
                }
            }
        }
    }

    /**
     * Deduces the WHO region from the continent.
     *
     * @param continent The continent
     * @return The corresponding WHO region, or null if no match
     */
    private Country.WHORegionEnum deduceWhoRegionFromContinent(Country.ContinentEnum continent) {
        if (continent == null) {
            return null;
        }
        
        switch (continent) {
            case NORTH_AMERICA:
            case SOUTH_AMERICA:
                return Country.WHORegionEnum.Americas;
            case AFRICA:
                return Country.WHORegionEnum.Africa;
            case EUROPE:
                return Country.WHORegionEnum.Europe;
            default:
                return null;
        }
    }

    /**
     * Tries to convert a string to a ContinentEnum value.
     * 
     * @param continentStr The string to convert
     * @return The enum value, or null if conversion fails
     */
    private Country.ContinentEnum parseContinent(String continentStr) {
        if (continentStr == null || continentStr.isEmpty()) {
            return null;
        }
        
        try {
            // First try to clean with the helper
            Country.ContinentEnum continent = cleanerHelper.cleanContinent(continentStr);
            if (continent != null) {
                return continent;
            }
            
            // Then try to parse directly as enum value
            return Country.ContinentEnum.valueOf(continentStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid continent string: '{}'. Error: {}", continentStr, e.getMessage());
            return null;
        }
    }
    
    /**
     * Tries to convert a string to a WHORegionEnum value.
     * 
     * @param whoRegionStr The string to convert
     * @return The enum value, or null if conversion fails
     */
    private Country.WHORegionEnum parseWhoRegion(String whoRegionStr) {
        if (whoRegionStr == null || whoRegionStr.isEmpty()) {
            return null;
        }
        
        try {
            // First try to clean with the helper
            Country.WHORegionEnum whoRegion = cleanerHelper.cleanWhoRegion(whoRegionStr);
            if (whoRegion != null) {
                return whoRegion;
            }
            
            // Then try to parse directly as enum value
            return Country.WHORegionEnum.valueOf(whoRegionStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid WHO region string: '{}'. Error: {}", whoRegionStr, e.getMessage());
            return null;
        }
    }

    /**
     * Updates the country cache from an iterable of saved/managed Country entities.
     *
     * @param savedCountries An iterable of Country entities, typically from saveAll
     */
    public void updateCache(Iterable<Country> savedCountries) {
        clear();
        if (savedCountries != null) {
            for (Country country : savedCountries) {
                if (country != null && country.getName() != null) {
                    put(country.getName(), country);
                }
            }
            logger.debug("Updated country cache with {} entities", size());
        }
    }
} 