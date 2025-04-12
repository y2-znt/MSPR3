package mspr.backend.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mspr.backend.BO.Region;
import mspr.backend.Repository.RegionRepository;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    public RegionRepository getRegionRepository() {
        return regionRepository;
    }

    public Optional<Region> getRegionById(Integer id) {
        return regionRepository.findById(id);
    }
    
    public Region getRegionByName(String name) {
        return regionRepository.findByName(name);
    }


}
