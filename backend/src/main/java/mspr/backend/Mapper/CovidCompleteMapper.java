package mspr.backend.Mapper;

import mspr.backend.DTO.CovidCompleteDto;
import mspr.backend.BO.*;
import mspr.backend.Helpers.CacheHelper;
import mspr.backend.Helpers.CleanerHelper;
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
    @Autowired private CacheHelper cacheHelper;
    @Autowired private CleanerHelper cleanerHelper;

    /**
     * Convertit un DTO CovidCompleteDto en un objet DiseaseCase (prêt à être persisté).
     * Cette méthode gère la création/recherche de Country, Region, Location, et lie la Disease (COVID-19).
     */
    public void dtoToEntity(HashMap<Integer, CovidCompleteDto> dtoMap) {

        //

        ArrayList<DiseaseCase> diseaseCases = new ArrayList<>();
        Disease covid19 = diseaseRepository.findByName("COVID-19");

        for (CovidCompleteDto dto : dtoMap.values()) {
            DiseaseCase diseaseCase = new DiseaseCase();
            diseaseCase.setDate(dto.getDate());
            diseaseCase.setConfirmedCases(dto.getConfirmed());
            diseaseCase.setDeaths(dto.getDeaths());
            diseaseCase.setRecovered(dto.getRecovered());
            diseaseCase.setDisease(covid19);

            Region region = cacheHelper.getRegionByName(dto.getProvinceState());
            if(region!=null){
                Location location = cacheHelper.getLocationByName(region.getName()+", location standard");
                diseaseCase.setLocation(location);
            }
            else{
                region = new Region();
                region.setName(dto.getProvinceState());
                Country country = cacheHelper.getCountryByName(dto.getCountryRegion());
                if(country != null){
                    region.setCountry(country);
                }
                else{
                    System.out.println("(CovidCompleteMapper) Creating new country for "+dto.getCountryRegion());
                    country = new Country();
                    country.setName(dto.getCountryRegion());
                    // TODO : set continent properly
                    country.setContinent(cleanerHelper.cleanContinent("null"));
                    country.setWhoRegion(cleanerHelper.cleanWhoRegion(dto.getWhoRegion()));
                    region.setCountry(country);
                    countryRepository.save(country);
                    cacheHelper.addCountryToCache(country.getName(), country);
                }


                Location location = new Location();
                location.setName(region.getName()+", location standard");
                location.setRegion(region);
                regionRepository.save(region);
                locationRepository.save(location);
                cacheHelper.addLocationToCache(location.getName(), location);
                cacheHelper.addRegionToCache(region.getName(), region);
            }
            diseaseCases.add(diseaseCase);
        }



        diseaseCaseRepository.saveAll(diseaseCases);

    }
}