package mspr.backend.ETL.Helpers;

import mspr.backend.ETL.BO.*;
import mspr.backend.ETL.Repository.*;
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


    public void setCountries(Map<String, Country> countries) {
        this.countries = countries;
    }

    public void setRegions(Map<String, Region> regions) {
        this.regions = regions;
    }

    public void setLocations(Map<String, Location> locations) {
        this.locations = locations;
    }
    public void setDiseases(Map<String, Disease> diseases) {
        this.diseases = diseases;
    }

}
