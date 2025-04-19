package mspr.backend.etl.helpers;

import mspr.backend.BO.Country;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class CleanerHelper {

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



    HashMap<String, Country.ContinentEnum> continentNameMap = new HashMap<>(
            Map.of(
                    "Africa", Country.ContinentEnum.AFRICA,
                    "Asia", Country.ContinentEnum.ASIA,
                    "Europe", Country.ContinentEnum.EUROPE,
                    "Australia/Oceania", Country.ContinentEnum.OCEANIA,
                    "North America", Country.ContinentEnum.NORTH_AMERICA,
                    "South America", Country.ContinentEnum.SOUTH_AMERICA
            )
    );

    HashMap<String, Country.WHORegionEnum> whoRegionNameMap = new HashMap<>(
            Map.of(
                    "Africa", Country.WHORegionEnum.Africa,
                    "Americas", Country.WHORegionEnum.Americas,
                    "EasternMediterranean", Country.WHORegionEnum.Eastern_Mediterranean,
                    "Europe", Country.WHORegionEnum.Europe,
                    "South-EastAsia", Country.WHORegionEnum.South_East_Asia,
                    "WesternPacific", Country.WHORegionEnum.Western_Pacific
            ));

    HashMap<String, Boolean> skipList = new HashMap<>(
            Map.of(
                    "Diamond Princess", true,
                    "Grand Princess", true
            )
    );


    public boolean isInSkipList(String string) {
        return skipList.containsKey(string);
    }

    public String cleanCountryName(String countryName) {

        if(countryNameMap.containsKey(countryName)){
            return countryNameMap.get(countryName);
        }

        return countryName.trim();
    }

    public String cleanRegionName(String regionName){
        return regionName.trim();
    }

    public String cleanLocationName(String locationName){
        return locationName.trim();
    }


    public Country.ContinentEnum cleanContinent(String continent) {
        if (continentNameMap.containsKey(continent)) {
            return continentNameMap.get(continent);
        }
        return null;
    }

    public Country.WHORegionEnum cleanWhoRegion(String whoRegion) {
        if (whoRegionNameMap.containsKey(whoRegion)) {
            return whoRegionNameMap.get(whoRegion);
        }
        return null;
    }
}
