package mspr.backend.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;


import mspr.backend.BO.Region;
import mspr.backend.Repository.RegionRepository;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    public Page<Region> getAllRegions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return regionRepository.findAll(pageable);
    }

    public Optional<Region> getRegionById(Integer id) {
        return regionRepository.findById(id);
    }
    
    public Region getRegionByName(String name) {
        return regionRepository.findByName(name);
    }

    public void createRegion(Region region) {
        regionRepository.save(region);
    }

    public Region updateRegion(Integer id, Region region) {
        if (regionRepository.existsById(id)) {
            region.setId(id);
            return regionRepository.save(region);
        } else {
            return null;
        }
    }

    public void deleteRegion(Integer id) {
        regionRepository.deleteById(id);
    }

    public void createRegion(Region region) {
        regionRepository.save(region);
    }

    public Region updateRegion(Integer id, Region region) {
        if (regionRepository.existsById(id)) {
            region.setId(id);
            return regionRepository.save(region);
        } else {
            return null;
        }
    }

    public void deleteRegion(Integer id) {
        regionRepository.deleteById(id);
    }


}
