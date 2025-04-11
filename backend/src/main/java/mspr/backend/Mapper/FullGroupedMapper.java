package mspr.backend.Mapper;

import mspr.backend.BO.*;
import mspr.backend.DTO.*;
import mspr.backend.Repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class FullGroupedMapper {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DiseaseRepository diseaseRepository;

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;

    @Autowired
    private RegionRepository regionRepository;

    /**
     *
     */
    public void dtoToEntity(HashMap<Integer, FullGroupedDto> dtoMap) {

        ArrayList<DiseaseCase> diseaseCases = new ArrayList<>();
        Disease covid19 = diseaseRepository.findByName("COVID-19");

        for (FullGroupedDto fullgroupedDto : dtoMap.values()) {
            DiseaseCase diseaseCase = new DiseaseCase();
            diseaseCase.setDate(fullgroupedDto.getDate());
            diseaseCase.setConfirmedCases(fullgroupedDto.getConfirmed());
            diseaseCase.setDeaths(fullgroupedDto.getDeaths());
            diseaseCase.setRecovered(fullgroupedDto.getRecovered());
            diseaseCase.setDisease(covid19);

            Region region = regionRepository.findByName(fullgroupedDto.getCountryRegion());
            // if region, set the location with the standard of that region.
            if (region != null) {
                diseaseCase.setLocation(locationRepository.findByName(region.getName()+", location standard"));
            }
           // if no region , get the country name
            else {
                Country country = countryRepository.findByName(fullgroupedDto.getCountryRegion());
                // if country exists, set the location with the standard of the standard region of that country.
                if (country!=null) {
                    diseaseCase.setLocation(locationRepository.findByName(country.getName()+", region standard, location standard"));
                } else{
                    System.out.println("ATTENTION (full_grouped): le pays ou region "+fullgroupedDto.getCountryRegion()+" n'existe pas dans la base de données.");
                }
            }
            diseaseCases.add(diseaseCase);
        }
        diseaseCaseRepository.saveAll(diseaseCases);

    }

}
