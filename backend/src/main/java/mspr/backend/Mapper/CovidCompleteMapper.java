package mspr.backend.Mapper;

import mspr.backend.DTO.CovidCompleteDto;
import mspr.backend.BO.*;
import mspr.backend.Repository.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class CovidCompleteMapper {

    @Autowired private CountryRepository countryRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private DiseaseRepository diseaseRepository;
    @Autowired private DiseaseCaseRepository diseaseCaseRepository;


    /**
     * Convertit un DTO CovidCompleteDto en un objet DiseaseCase (prêt à être persisté).
     * Cette méthode gère la création/recherche de Country, Region, Location, et lie la Disease (COVID-19).
     */
    public void dtoToEntity(HashMap<Integer, CovidCompleteDto> dtoMap) {

        ArrayList<DiseaseCase> diseaseCases = new ArrayList<>();
        Disease covid19 = diseaseRepository.findByName("COVID-19");

        for (CovidCompleteDto dto : dtoMap.values()) {
            DiseaseCase diseaseCase = new DiseaseCase();
            diseaseCase.setDate(dto.getDate());
            diseaseCase.setConfirmedCases(dto.getConfirmed());
            diseaseCase.setDeaths(dto.getDeaths());
            diseaseCase.setRecovered(dto.getRecovered());
            diseaseCase.setDisease(covid19);
            Region region = regionRepository.findByName(dto.getCountryRegion());
            // if region, set the location with the standard of that region.
            if (region != null) {
                diseaseCase.setLocation(locationRepository.findByName(region.getName()+", location standard"));
            }
            // if no region , get the country name
            else {
                Country country = countryRepository.findByName(dto.getCountryRegion());
                // if country exists, set the location with the standard of the standard region of that country.
                if (country!=null) {
                    diseaseCase.setLocation(locationRepository.findByName(country.getName()+", region standard, location standard"));
                } else{
                    System.out.println("ATTENTION (full_grouped): le pays ou region "+dto.getCountryRegion()+" n'existe pas dans la base de données.");
                }
            }
            diseaseCases.add(diseaseCase);
        }



        diseaseCaseRepository.saveAll(diseaseCases);

    }
}