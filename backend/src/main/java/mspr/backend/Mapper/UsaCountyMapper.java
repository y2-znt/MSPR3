package mspr.backend.Mapper;

import mspr.backend.DTO.UsaCountyDto;
import mspr.backend.BO.*;
import mspr.backend.Helpers.CacheHelper;
import org.springframework.stereotype.Component;


@Component
public class UsaCountyMapper {

    public DiseaseCase fromDto(UsaCountyDto dto, CacheHelper cache) {
        if (dto == null) {
            return null;
        }
        // Nettoyage des noms de pays/région/lieu (trim et alias)
        String countryName = dto.getCountryRegion() != null ? dto.getCountryRegion().trim() : "";

        String regionName = dto.getProvinceState() != null ? dto.getProvinceState().trim() : "";
        String locationName = dto.getCounty() != null ? dto.getCounty().trim() : "";
        // Si le champ location (comté) est vide, on utilise le nom de région comme location
        if (locationName.isEmpty()) {
            locationName = regionName;
        }

        // Récupération ou création des entités de référence à partir du cache
        Country country = cache.getOrCreateCountry(countryName);
        Region region = cache.getOrCreateRegion(country, regionName);
        Location location = cache.getOrCreateLocation(region, locationName);

        // Mapping du DTO vers l'entité DiseaseCase
        DiseaseCase diseaseCase = new DiseaseCase();
        diseaseCase.setLocation(location);
        diseaseCase.setDate(dto.getDate());         // suppose qu'on a déjà un objet LocalDate/Date dans le DTO
        diseaseCase.setConfirmedCases(dto.getConfirmed());
        diseaseCase.setDeaths(dto.getDeaths());
        return diseaseCase;
    }
}



