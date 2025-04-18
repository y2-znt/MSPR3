package mspr.backend.etl.helpers;

import mspr.backend.BO.Country;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class CleanerHelper {

    HashMap<String, String> countryNameMap = new HashMap<>(
            Map.of(
                    "United States", "USA",
                    "US", "USA",
                    "United Kingdom", "UK",
                    "South Korea", "Korea, South",
                    "North Korea", "Korea, North",
                    "Czech Republic", "Czechia",
                    "Ivory Coast", "Côte d'Ivoire",
                    "Republic of the Congo", "Congo",
                    "Democratic Republic of the Congo", "Congo, Democratic Republic of the"
            )
    );


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
