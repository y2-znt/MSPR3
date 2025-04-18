package mspr.backend.ETL.Mapper;

import mspr.backend.BO.*;
import mspr.backend.ETL.Helpers.*;
import mspr.backend.ETL.DTO.WorldometerDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class WorldometerMapper {

    private final CacheHelper cacheHelper;
    private final CleanerHelper cleanerHelper;

    @Autowired
    public WorldometerMapper(CacheHelper cacheHelper, CleanerHelper cleanerHelper) {
        this.cacheHelper = cacheHelper;
        this.cleanerHelper = cleanerHelper;
    }

    /**
     * Conteneur pour le résultat (Country, Region, Location) retourné par le mapper Worldometer.
     */
    public static class CountryRegionLocation {
        private final Country country;
        private final Region region;
        private final Location location;
        public CountryRegionLocation(Country country, Region region, Location location) {
            this.country = country;
            this.region = region;
            this.location = location;
        }
        public Country getCountry() { return country; }
        public Region getRegion() { return region; }
        public Location getLocation() { return location; }
    }

    /**
     * Convertit un WorldometerDto en triple Country/Region/Location.
     * Aucune sauvegarde en base n'est effectuée ici.
     */
    public CountryRegionLocation toEntity(WorldometerDto dto) {
        // Nettoyage du nom de pays
        String countryName = cleanerHelper.cleanCountryName(dto.getCountry());
        Country country = cacheHelper.getOrCreateCountry(countryName);
        Region region = cacheHelper.getOrCreateRegion(country, "standard");;
        Location location = cacheHelper.getOrCreateLocation(region, "standard");
        // Mettre à jour éventuellement les attributs statiques du pays (ex: population)
        country.setPopulation(dto.getPopulation());
        // (D'autres champs de dto, comme totalCases, peuvent être utilisés si nécessaire)
        return new CountryRegionLocation(country, region, location);
    }
}
