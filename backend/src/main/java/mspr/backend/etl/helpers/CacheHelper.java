package mspr.backend.etl.helpers;

import mspr.backend.BO.*;
import mspr.backend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component
public class CacheHelper {
    private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);

    @Autowired
    private DiseaseRepository diseaseRepository;

    @Autowired
    private CleanerHelper cleanerHelper;

    private Map<String, Country> countries = new HashMap<>();
    private Map<String, Region> regions = new HashMap<>();
    private Map<String, Location> locations = new HashMap<>();
    // Cache pour détecter les noms de location en double à travers différentes régions
    private Map<String, Location> locationsByNameOnly = new HashMap<>();
    private Map<String, Disease> diseases = new HashMap<>();

    // Constants for standard names
    private static final String STANDARD_REGION_SUFFIX = "region standard";
    private static final String STANDARD_LOCATION_SUFFIX = "location standard";
    private static final String STANDARD = "standard";

    /**
     * Gets or creates a Country entity with just a name
     * @param countryName The name of the country
     * @return The Country entity
     */
    public Country getOrCreateCountry(String countryName) {
        return getOrCreateCountry(countryName, null, null);
    }

    /**
     * Déduit la région WHO à partir du continent
     * @param continent Le continent
     * @return La région WHO correspondante ou null si pas de correspondance
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
     * Tente de convertir une chaîne en une valeur d'enum ContinentEnum
     * 
     * @param continentStr La chaîne à convertir
     * @return La valeur d'enum ou null si la conversion échoue
     */
    private Country.ContinentEnum parseContinent(String continentStr) {
        if (continentStr == null || continentStr.isEmpty()) {
            return null;
        }
        
        try {
            // D'abord essayer de nettoyer avec le helper
            Country.ContinentEnum continent = cleanerHelper.cleanContinent(continentStr);
            if (continent != null) {
                return continent;
            }
            
            // Ensuite essayer de parser directement comme valeur d'enum
            return Country.ContinentEnum.valueOf(continentStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid continent string: '{}'. Error: {}", continentStr, e.getMessage());
            return null;
        }
    }
    
    /**
     * Tente de convertir une chaîne en une valeur d'enum WHORegionEnum
     * 
     * @param whoRegionStr La chaîne à convertir
     * @return La valeur d'enum ou null si la conversion échoue
     */
    private Country.WHORegionEnum parseWhoRegion(String whoRegionStr) {
        if (whoRegionStr == null || whoRegionStr.isEmpty()) {
            return null;
        }
        
        try {
            // D'abord essayer de nettoyer avec le helper
            Country.WHORegionEnum whoRegion = cleanerHelper.cleanWhoRegion(whoRegionStr);
            if (whoRegion != null) {
                return whoRegion;
            }
            
            // Ensuite essayer de parser directement comme valeur d'enum
            return Country.WHORegionEnum.valueOf(whoRegionStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid WHO region string: '{}'. Error: {}", whoRegionStr, e.getMessage());
            return null;
        }
    }

    /**
     * Gets or creates a Country entity with name, continent and WHO region
     * @param countryName The name of the country
     * @param continentStr The continent name (will be cleaned)
     * @param whoRegionStr The WHO region name (will be cleaned)
     * @return The Country entity
     */
    public Country getOrCreateCountry(String countryName, String continentStr, String whoRegionStr) {
        if (countryName == null || countryName.isEmpty()) {
            logger.debug("Null or empty country name, returning null");
            return null;
        }
        
        // Clé simple pour le pays (nom du pays)
        String countryKey = countryName;
        Country country = countries.get(countryKey);
        
        if (country == null) {
            country = new Country();
            country.setName(countryName);
            
            // Check for special case continent first
            Country.ContinentEnum specialContinent = cleanerHelper.getSpecialCaseContinent(countryName);
            if (specialContinent != null) {
                country.setContinent(specialContinent);
                logger.debug("Set special case continent {} for country {}", specialContinent, countryName);
            } else {
                // Nettoyer et définir le continent si fourni
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
                // Nettoyer et définir la région WHO si fournie
                Country.WHORegionEnum whoRegion = parseWhoRegion(whoRegionStr);
                if (whoRegion != null) {
                    country.setWhoRegion(whoRegion);
                    logger.debug("Set WHO region {} for country {}", whoRegion, countryName);
                } else {
                    logger.debug("No valid WHO region found for string: '{}'", whoRegionStr);
                }
            }
            
            // Si la région WHO est null mais que le continent est défini, déduire la région WHO
            if (country.getWhoRegion() == null && country.getContinent() != null) {
                Country.WHORegionEnum whoRegion = deduceWhoRegionFromContinent(country.getContinent());
                if (whoRegion != null) {
                    country.setWhoRegion(whoRegion);
                    logger.debug("Deduced WHO region {} from continent {} for country {}", 
                                whoRegion, country.getContinent(), countryName);
                }
            }
            
            countries.put(countryKey, country);
            logger.debug("Created new country: {} with continent: {} and WHO region: {}", 
                        countryName, country.getContinent(), country.getWhoRegion());
            
            // Créer automatiquement une région standard pour le nouveau pays
            getOrCreateRegion(country, countryName + " - " + STANDARD_REGION_SUFFIX);
        } else {
            // Check for special cases for existing countries that might be missing data
            if (country.getContinent() == null) {
                Country.ContinentEnum specialContinent = cleanerHelper.getSpecialCaseContinent(countryName);
                if (specialContinent != null) {
                    country.setContinent(specialContinent);
                    logger.debug("Updated existing country {} with special case continent {}", 
                               countryName, specialContinent);
                }
            }
            
            if (country.getWhoRegion() == null) {
                Country.WHORegionEnum specialWhoRegion = cleanerHelper.getSpecialCaseWhoRegion(countryName);
                if (specialWhoRegion != null) {
                    country.setWhoRegion(specialWhoRegion);
                    logger.debug("Updated existing country {} with special case WHO region {}", 
                               countryName, specialWhoRegion);
                } else if (country.getContinent() != null) {
                    // Si le pays existe déjà mais que la région WHO est null, essayer de la déduire à partir du continent
                    Country.WHORegionEnum whoRegion = deduceWhoRegionFromContinent(country.getContinent());
                    if (whoRegion != null) {
                        country.setWhoRegion(whoRegion);
                        logger.debug("Updated existing country {} with deduced WHO region {} from continent {}", 
                                   countryName, whoRegion, country.getContinent());
                    }
                }
            }
        }
        return country;
    }

    public Region getOrCreateRegion(Country country, String regionName) {
        if (country == null || regionName == null || regionName.isEmpty()) {
            return null;
        }
        
        // Si le nom de la région est juste "standard", le remplacer par le nom du pays + "region standard"
        if (STANDARD.equalsIgnoreCase(regionName)) {
            regionName = country.getName() + " - " + STANDARD_REGION_SUFFIX;
        }
        
        // Clé composite pour la région: "CountryName|RegionName"
        String regionKey = country.getName() + "|" + regionName;
        Region region = regions.get(regionKey);
        if (region == null) {
            region = new Region();
            region.setName(regionName);
            region.setCountry(country);
            regions.put(regionKey, region);
            
            // Créer automatiquement une location standard pour la nouvelle région
            getOrCreateLocation(region, regionName + " - " + STANDARD_LOCATION_SUFFIX);
        }
        return region;
    }

    public Location getOrCreateLocation(Region region, String locationName) {
        if (region == null || locationName == null || locationName.isEmpty()) {
            return null;
        }
        
        // Si le nom de la location est juste "standard", le remplacer par le nom de la région + "location standard"
        if (STANDARD.equalsIgnoreCase(locationName)) {
            locationName = region.getName() + " - " + STANDARD_LOCATION_SUFFIX;
        }
        
        // Clé composite pour la location: "CountryName|RegionName|LocationName"
        String locationKey = region.getCountry().getName() + "|" + region.getName() + "|" + locationName;
        Location location = locations.get(locationKey);
        
        if (location == null) {
            // Vérifier si un nom identique existe dans une autre région
            Location existingByName = locationsByNameOnly.get(locationName);
            if (existingByName != null && existingByName.getRegion() != region) {
                // Si une location avec ce nom existe déjà dans une autre région, modifier le nom pour le rendre unique
                String uniqueName = region.getName() + " - " + locationName;
                
                // Vérifier si cette version unique existe déjà
                String uniqueKey = region.getCountry().getName() + "|" + region.getName() + "|" + uniqueName;
                Location existingUnique = locations.get(uniqueKey);
                
                if (existingUnique != null) {
                    return existingUnique;
                }
                
                location = new Location();
                location.setName(uniqueName);
                location.setRegion(region);
                locations.put(uniqueKey, location);
                locationsByNameOnly.put(uniqueName, location);
            } else {
                // Nom original unique ou dans la même région, on peut l'utiliser tel quel
                location = new Location();
                location.setName(locationName);
                location.setRegion(region);
                locations.put(locationKey, location);
                locationsByNameOnly.put(locationName, location);
            }
        }
        
        return location;
    }

    /**
     * Gère les cas où le nom de la région ou de la location est vide,
     * en utilisant le nom parent suivi du suffixe correspondant
     */
    public Region getOrCreateRegionWithEmptyHandling(Country country, String regionName) {
        if (country == null) {
            return null;
        }
        
        // Si le nom de la région est vide ou standard, utiliser le nom du pays + suffixe standard
        if (regionName == null || regionName.isEmpty() || STANDARD.equalsIgnoreCase(regionName)) {
            regionName = country.getName() + " - " + STANDARD_REGION_SUFFIX;
        }
        
        return getOrCreateRegion(country, regionName);
    }

    /**
     * Gère les cas où le nom de la location est vide,
     * en utilisant le nom parent suivi du suffixe correspondant
     */
    public Location getOrCreateLocationWithEmptyHandling(Region region, String locationName) {
        if (region == null) {
            return null;
        }
        
        // Si le nom de la location est vide ou standard, utiliser le nom de la région + suffixe standard
        if (locationName == null || locationName.isEmpty() || STANDARD.equalsIgnoreCase(locationName)) {
            locationName = region.getName() + " - " + STANDARD_LOCATION_SUFFIX;
        }
        
        return getOrCreateLocation(region, locationName);
    }

    /**
     * Generates the standard cache key for a location.
     *
     * @param countryName  The name of the country.
     * @param regionName   The name of the region.
     * @param locationName The name of the location.
     * @return The composite key used for the location cache, or null if any part is null/empty.
     */
    public String getLocationKey(String countryName, String regionName, String locationName) {
        if (countryName == null || countryName.isEmpty() ||
            regionName == null || regionName.isEmpty() ||
            locationName == null || locationName.isEmpty()) {
            return null;
        }
        return countryName + "|" + regionName + "|" + locationName;
    }

    public Disease getOrCreateDisease(String diseaseName) {
        if (diseaseName == null || diseaseName.isEmpty()) {
            return null;
        }
        // Clé simple pour la maladie (nom de la maladie)
        String diseaseKey = diseaseName;
        Disease disease = diseases.get(diseaseKey);
        if (disease == null) {
            disease = new Disease();
            disease.setName(diseaseName);
            diseases.put(diseaseKey, disease);
        }
        return disease;
    }
    public void addDiseaseToCache(String diseaseName, Disease disease) {
        if (diseaseName != null && !diseaseName.isEmpty() && disease != null) {
            diseases.put(diseaseName, disease);
        }
    }


    public Map<String, Country> getCountries(){
        return this.countries;
    }
    public Map<String, Region> getRegions(){
        return regions;
    }
    public Map<String, Location> getLocations(){
        return locations;
    }
    public Map<String, Disease> getDiseases(){
        return diseases;
    }


    /**
     * Updates the country cache from an iterable of saved/managed Country entities.
     * The internal map is cleared and rebuilt.
     * @param savedCountries An iterable (e.g., List) of Country entities, typically from saveAll.
     */
    public void setCountries(Iterable<Country> savedCountries) {
        this.countries.clear();
        if (savedCountries != null) {
            for (Country country : savedCountries) {
                if (country != null && country.getName() != null) {
                    this.countries.put(country.getName(), country);
                }
            }
        }
    }

    /**
     * Updates the region cache from an iterable of saved/managed Region entities.
     * The internal map is cleared and rebuilt using composite keys ("CountryName|RegionName").
     * @param savedRegions An iterable (e.g., List) of Region entities, typically from saveAll.
     */
    public void setRegions(Iterable<Region> savedRegions) {
        this.regions.clear();
        if (savedRegions != null) {
            for (Region region : savedRegions) {
                // Ensure necessary fields for the key are present
                if (region != null && region.getName() != null && region.getCountry() != null && region.getCountry().getName() != null) {
                    String regionKey = region.getCountry().getName() + "|" + region.getName();
                    this.regions.put(regionKey, region);
                }
            }
        }
    }

    /**
     * Updates the location cache from an iterable of saved/managed Location entities.
     * The internal map is cleared and rebuilt using composite keys ("CountryName|RegionName|LocationName").
     * @param savedLocations An iterable (e.g., List) of Location entities, typically from saveAll.
     */
    public void setLocations(Iterable<Location> savedLocations) {
        this.locations.clear();
        this.locationsByNameOnly.clear();
        if (savedLocations != null) {
            for (Location location : savedLocations) {
                // Ensure necessary fields for the key are present
                if (location != null && location.getName() != null &&
                    location.getRegion() != null && location.getRegion().getName() != null &&
                    location.getRegion().getCountry() != null && location.getRegion().getCountry().getName() != null)
                {
                    String locationKey = location.getRegion().getCountry().getName() + "|" +
                                         location.getRegion().getName() + "|" +
                                         location.getName();
                    this.locations.put(locationKey, location);
                    this.locationsByNameOnly.put(location.getName(), location);
                }
            }
        }
    }

    /**
     * Updates the disease cache from an iterable of saved/managed Disease entities.
     * The internal map is cleared and rebuilt.
     * @param savedDiseases An iterable (e.g., List) of Disease entities, typically from saveAll.
     */
    public void setDiseases(Iterable<Disease> savedDiseases) {
        this.diseases.clear();
        if (savedDiseases != null) {
            for (Disease disease : savedDiseases) {
                if (disease != null && disease.getName() != null) {
                    this.diseases.put(disease.getName(), disease);
                }
            }
        }
    }
}
