package mspr.backend.Mapper;

import mspr.backend.BO.*;
import mspr.backend.DTO.*;
import mspr.backend.Helpers.CacheHelper;
import mspr.backend.Helpers.CleanerHelper;
import mspr.backend.Repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class FullGroupedMapper {


    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;

    @Autowired
    private CacheHelper cacheHelper;

    @Autowired
    private CleanerHelper cleanerHelper;
    @Autowired
    private CountryRepository countryRepository;

    /**
     *
     */
    public void dtoToEntity(HashMap<Integer, FullGroupedDto> dtoMap) {

        ArrayList<DiseaseCase> diseaseCases = new ArrayList<>();
        Disease covid19 = cacheHelper.getDiseaseByName("COVID-19");

        for (FullGroupedDto fullgroupedDto : dtoMap.values()) {
            DiseaseCase diseaseCase = new DiseaseCase();
            diseaseCase.setDate(fullgroupedDto.getDate());
            diseaseCase.setConfirmedCases(fullgroupedDto.getConfirmed());
            diseaseCase.setDeaths(fullgroupedDto.getDeaths());
            diseaseCase.setRecovered(fullgroupedDto.getRecovered());
            diseaseCase.setDisease(covid19);

            Country country = cacheHelper.getCountryByName(fullgroupedDto.getCountryRegion());
            // if country, set the location with the standard of that country.
            if(country != null){
                diseaseCase.setLocation(cacheHelper.getLocationByName(country.getName()+", region standard, location standard"));
            }
            else{
                Region region = cacheHelper.getRegionByName(fullgroupedDto.getCountryRegion());
                // if region, set the location with the standard of that region.
                if (region != null) {
                    diseaseCase.setLocation(cacheHelper.getLocationByName(cleanerHelper.cleanRegionName(region.getName())+", location standard"));
                }
                // if no region , that means we must create a new country or region... No way to tell which one
                else {
                        System.out.println("Creating new country for "+fullgroupedDto.getCountryRegion());
                    country = new Country();
                    country.setName(fullgroupedDto.getCountryRegion());
                    // TODO : set continent properly
                    country.setContinent(cleanerHelper.cleanContinent("null"));
                    country.setWhoRegion(cleanerHelper.cleanWhoRegion(fullgroupedDto.getWhoRegion()));
                    countryRepository.save(country);
                    cacheHelper.addCountryToCache(country.getName(), country);
                }
            }

            diseaseCases.add(diseaseCase);
        }
        diseaseCaseRepository.saveAll(diseaseCases);

    }

}
