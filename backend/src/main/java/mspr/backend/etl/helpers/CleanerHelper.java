package mspr.backend.etl.helpers;

import mspr.backend.entity.Country;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class CleanerHelper {
    private static final Logger logger = LoggerFactory.getLogger(CleanerHelper.class);

    private static final Map<String, String> countryNameMap = new HashMap<>();
    static {
        countryNameMap.put("United States", "USA");
        countryNameMap.put("US", "USA");
        countryNameMap.put("UK", "United Kingdom");
        countryNameMap.put("UAE", "United Arab Emirates");
        countryNameMap.put("Myanmar", "Burma");
        countryNameMap.put("Palestine", "West Bank and Gaza");
        countryNameMap.put("Korea, South", "South Korea");
        countryNameMap.put("S. Korea", "South Korea");
        countryNameMap.put("Korea, North", "North Korea");
        countryNameMap.put("Czech Republic", "Czechia");
        countryNameMap.put("Ivory Coast", "Côte d'Ivoire");
        countryNameMap.put("Cote d'Ivoire", "Côte d'Ivoire");
        countryNameMap.put("Taiwan*", "Taiwan");
        countryNameMap.put("CAR", "Central African Republic");
        countryNameMap.put("DRC", "Congo (Kinshasa)");
        countryNameMap.put("Congo", "Congo (Brazzaville)");
        countryNameMap.put("Vatican City", "Holy See");
        countryNameMap.put("St. Vincent Grenadines", "Saint Vincent and the Grenadines");
    }

    // Map des noms de continents vers les valeurs d'enum ContinentEnum
    private static final Map<String, Country.ContinentEnum> continentNameMap = new HashMap<>();
    static {
        continentNameMap.put("Africa", Country.ContinentEnum.AFRICA);
        continentNameMap.put("Asia", Country.ContinentEnum.ASIA);
        continentNameMap.put("Europe", Country.ContinentEnum.EUROPE);
        continentNameMap.put("Australia/Oceania", Country.ContinentEnum.OCEANIA);
        continentNameMap.put("Oceania", Country.ContinentEnum.OCEANIA);
        continentNameMap.put("Australia", Country.ContinentEnum.OCEANIA);
        continentNameMap.put("North America", Country.ContinentEnum.NORTH_AMERICA);
        continentNameMap.put("South America", Country.ContinentEnum.SOUTH_AMERICA);
        // Ajouter des alias pour les cas spéciaux
        continentNameMap.put("Americas", Country.ContinentEnum.NORTH_AMERICA); // Simplification
        continentNameMap.put("NA", Country.ContinentEnum.NORTH_AMERICA); // Abréviation
        continentNameMap.put("SA", Country.ContinentEnum.SOUTH_AMERICA); // Abréviation
        continentNameMap.put("EU", Country.ContinentEnum.EUROPE); // Abréviation
        continentNameMap.put("AF", Country.ContinentEnum.AFRICA); // Abréviation
        continentNameMap.put("AS", Country.ContinentEnum.ASIA); // Abréviation
        continentNameMap.put("OC", Country.ContinentEnum.OCEANIA); // Abréviation
    }

    // Map des noms de régions WHO vers les valeurs d'enum WHORegionEnum
    private static final Map<String, Country.WHORegionEnum> whoRegionNameMap = new HashMap<>();
    static {
        whoRegionNameMap.put("Africa", Country.WHORegionEnum.Africa);
        whoRegionNameMap.put("Americas", Country.WHORegionEnum.Americas);
        whoRegionNameMap.put("EasternMediterranean", Country.WHORegionEnum.Eastern_Mediterranean);
        whoRegionNameMap.put("Eastern Mediterranean", Country.WHORegionEnum.Eastern_Mediterranean);
        whoRegionNameMap.put("Europe", Country.WHORegionEnum.Europe);
        whoRegionNameMap.put("South-EastAsia", Country.WHORegionEnum.South_East_Asia);
        whoRegionNameMap.put("South-East Asia", Country.WHORegionEnum.South_East_Asia);
        whoRegionNameMap.put("SouthEastAsia", Country.WHORegionEnum.South_East_Asia);
        whoRegionNameMap.put("South East Asia", Country.WHORegionEnum.South_East_Asia);
        whoRegionNameMap.put("WesternPacific", Country.WHORegionEnum.Western_Pacific);
        whoRegionNameMap.put("Western Pacific", Country.WHORegionEnum.Western_Pacific);
        whoRegionNameMap.put("Western-Pacific", Country.WHORegionEnum.Western_Pacific);
    }

    // Liste des entités à ignorer lors de l'importation
    private static final Map<String, Boolean> skipList = new HashMap<>();
    static {
        // Navires de croisière
        skipList.put("Diamond Princess", true);
        skipList.put("Grand Princess", true);
    }

    // Special cases mapping for countries with specific WHO regions
    private static final Map<String, Country.WHORegionEnum> specialWhoRegionMap = new HashMap<>();
    static {
        // Special cases from the database
        specialWhoRegionMap.put("French Polynesia", Country.WHORegionEnum.Western_Pacific);
        specialWhoRegionMap.put("Brunei", Country.WHORegionEnum.Western_Pacific);
        specialWhoRegionMap.put("New Caledonia", Country.WHORegionEnum.Western_Pacific);
        specialWhoRegionMap.put("Macao", Country.WHORegionEnum.Western_Pacific);
        specialWhoRegionMap.put("China", Country.WHORegionEnum.Western_Pacific);
        specialWhoRegionMap.put("Kosovo", Country.WHORegionEnum.Europe);
    }
    
    // Special cases mapping for countries with specific continents
    private static final Map<String, Country.ContinentEnum> specialContinentMap = new HashMap<>();
    static {
        // Special cases from the database
        specialContinentMap.put("China", Country.ContinentEnum.ASIA);
        specialContinentMap.put("Kosovo", Country.ContinentEnum.EUROPE);
    }

    public boolean isInSkipList(String string) {
        if (string == null) {
            return false;
        }
        boolean result = skipList.containsKey(string.trim());
        if (result) {
            logger.debug("Found '{}' in skip list", string);
        }
        return result;
    }

    public String cleanCountryName(String countryName) {
        if (countryName == null || countryName.isEmpty()) {
            return countryName;
        }

        if(countryNameMap.containsKey(countryName)){
            String cleaned = countryNameMap.get(countryName);
            logger.debug("Cleaned country name: '{}' -> '{}'", countryName, cleaned);
            return cleaned;
        }

        return countryName.trim();
    }

    public String cleanRegionName(String regionName){
        if (regionName == null || regionName.isEmpty()) {
            return regionName;
        }
        return regionName.trim();
    }

    public String cleanLocationName(String locationName){
        if (locationName == null || locationName.isEmpty()) {
            return locationName;
        }
        return locationName.trim();
    }


    public Country.ContinentEnum cleanContinent(String continent) {
        if (continent == null || continent.isEmpty()) {
            return null;
        }
        
        // Essayer le map de noms de continents, insensible à la casse
        for (Map.Entry<String, Country.ContinentEnum> entry : continentNameMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(continent.trim())) {
                Country.ContinentEnum value = entry.getValue();
                logger.debug("Matched continent '{}' to enum {}", continent, value);
                return value;
            }
        }
        
        // Si pas trouvé, essayer de convertir la chaîne en enum directement
        try {
            Country.ContinentEnum value = Country.ContinentEnum.valueOf(continent.trim().toUpperCase());
            logger.debug("Parsed continent string '{}' to enum {}", continent, value);
            return value;
        } catch (IllegalArgumentException e) {
            logger.warn("Could not convert '{}' to a continent enum value", continent);
            return null;
        }
    }

    public Country.WHORegionEnum cleanWhoRegion(String whoRegion) {
        if (whoRegion == null || whoRegion.isEmpty()) {
            return null;
        }
        
        // Essayer le map de noms de régions WHO, insensible à la casse
        for (Map.Entry<String, Country.WHORegionEnum> entry : whoRegionNameMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(whoRegion.trim())) {
                Country.WHORegionEnum value = entry.getValue();
                logger.debug("Matched WHO region '{}' to enum {}", whoRegion, value);
                return value;
            }
        }
        
        // Si pas trouvé, essayer de convertir la chaîne en enum directement
        try {
            Country.WHORegionEnum value = Country.WHORegionEnum.valueOf(whoRegion.trim().replace(' ', '_').replace('-', '_'));
            logger.debug("Parsed WHO region string '{}' to enum {}", whoRegion, value);
            return value;
        } catch (IllegalArgumentException e) {
            logger.warn("Could not convert '{}' to a WHO region enum value", whoRegion);
            return null;
        }
    }

    /**
     * Gets the WHO region for a special case country if it exists
     * @param countryName The name of the country
     * @return The WHO region enum or null if not a special case
     */
    public Country.WHORegionEnum getSpecialCaseWhoRegion(String countryName) {
        if (countryName == null || countryName.isEmpty()) {
            return null;
        }
        
        // Try direct match
        Country.WHORegionEnum region = specialWhoRegionMap.get(countryName);
        if (region != null) {
            logger.debug("Found special case WHO region for country {}: {}", countryName, region);
            return region;
        }
        
        // Try cleaned name
        String cleanedName = cleanCountryName(countryName);
        region = specialWhoRegionMap.get(cleanedName);
        if (region != null) {
            logger.debug("Found special case WHO region for cleaned country name {}: {}", cleanedName, region);
            return region;
        }
        
        return null;
    }
    
    /**
     * Gets the continent for a special case country if it exists
     * @param countryName The name of the country
     * @return The continent enum or null if not a special case
     */
    public Country.ContinentEnum getSpecialCaseContinent(String countryName) {
        if (countryName == null || countryName.isEmpty()) {
            return null;
        }
        
        // Try direct match
        Country.ContinentEnum continent = specialContinentMap.get(countryName);
        if (continent != null) {
            logger.debug("Found special case continent for country {}: {}", countryName, continent);
            return continent;
        }
        
        // Try cleaned name
        String cleanedName = cleanCountryName(countryName);
        continent = specialContinentMap.get(cleanedName);
        if (continent != null) {
            logger.debug("Found special case continent for cleaned country name {}: {}", cleanedName, continent);
            return continent;
        }
        
        return null;
    }
}
