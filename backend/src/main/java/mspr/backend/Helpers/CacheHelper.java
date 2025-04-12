package mspr.backend.Helpers;

import mspr.backend.BO.*;
import mspr.backend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class CacheHelper {

    @Autowired private CountryRepository countryRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private DiseaseRepository diseaseRepository;
//    @Autowired private DiseaseCaseRepository diseaseCaseRepository;

    HashMap<String, Disease> diseaseCache = new HashMap<>();
    HashMap<String, Country> countryCache = new HashMap<>();
    HashMap<String, Region> regionCache = new HashMap<>();
    HashMap<String, Location> locationCache = new HashMap<>();


    public Disease getDiseaseByName(String name) {
        if (diseaseCache.containsKey(name)) {
            return diseaseCache.get(name);
        } else {
            Disease disease = diseaseRepository.findByName(name);
            if (disease != null) {
                diseaseCache.put(name, disease);
            }
            return disease;
        }
    }


    public Country getCountryByName(String name) {
        if (countryCache.containsKey(name)) {
            return countryCache.get(name);
        } else {
            Country country = countryRepository.findByName(name);
            if (country != null) {
                countryCache.put(name, country);
            }
            return country;
        }
    }
    public Region getRegionByName(String name) {
        if (regionCache.containsKey(name)) {
            return regionCache.get(name);
        } else {
            Region region = regionRepository.findByName(name);
            regionCache.put(name, region);
            return region;
        }
    }
    public Location getLocationByName(String name) {
        if (locationCache.containsKey(name)) {
            return locationCache.get(name);
        } else {
            System.out.println("CacheHelper: locationRepository.findByName: "+name);
            Location location = locationRepository.findByName(name);
            if (location != null) {
                locationCache.put(name, location);
            }
            return location;
        }
    }

    public void addLocationToCache(String name, Location location) {
        locationCache.put(name, location);
    }
    public void addRegionToCache(String name, Region region) {
        regionCache.put(name, region);
    }
    public void addCountryToCache(String name, Country country) {
        countryCache.put(name, country);
    }
    public void addDiseaseToCache(String name, Disease disease) {
        diseaseCache.put(name, disease);
    }

    // TODO : ajouter une fonction pour get or create une region + créer une location ,
    // TODO : et même chose pour country

    public void clearCache() {
        diseaseCache.clear();
        countryCache.clear();
        regionCache.clear();
        locationCache.clear();
    }
}
