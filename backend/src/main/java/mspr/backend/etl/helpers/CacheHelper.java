package mspr.backend.etl.helpers;

import mspr.backend.BO.*;
import mspr.backend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CacheHelper {

    @Autowired
    private DiseaseRepository diseaseRepository;

    private Map<String, Country> countries = new HashMap<>();
    private Map<String, Region> regions = new HashMap<>();
    private Map<String, Location> locations = new HashMap<>();
    private Map<String, Disease> diseases = new HashMap<>();

    public Country getOrCreateCountry(String countryName) {
        if (countryName == null || countryName.isEmpty()) {
            return null;
        }
        // Clé simple pour le pays (nom du pays)
        String countryKey = countryName;
        Country country = countries.get(countryKey);
        if (country == null) {
            country = new Country();
            country.setName(countryName);
            countries.put(countryKey, country);
        }
        return country;
    }

    public Region getOrCreateRegion(Country country, String regionName) {
        if (country == null || regionName == null || regionName.isEmpty()) {
            return null;
        }
        // Clé composite pour la région: "CountryName|RegionName"
        String regionKey = country.getName() + "|" + regionName;
        Region region = regions.get(regionKey);
        if (region == null) {
            region = new Region();
            region.setName(regionName);
            region.setCountry(country);
            regions.put(regionKey, region);
        }
        return region;
    }

    public Location getOrCreateLocation(Region region, String locationName) {
        if (region == null || locationName == null || locationName.isEmpty()) {
            return null;
        }
        // Clé composite pour la location: "CountryName|RegionName|LocationName"
        String locationKey = region.getCountry().getName() + "|" + region.getName() + "|" + locationName;
        Location location = locations.get(locationKey);
        if (location == null) {
            location = new Location();
            location.setName(locationName);
            location.setRegion(region);
            locations.put(locationKey, location);
        }
        return location;
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
