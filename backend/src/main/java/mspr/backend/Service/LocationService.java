package mspr.backend.Service;



import org.springframework.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mspr.backend.BO.Location;
import mspr.backend.Repository.LocationRepository;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public Page<Location> getAllLocations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return locationRepository.findAll(pageable);
    }

    public Location getLocationById(Integer id) {
        return locationRepository.findById(id).orElse(null);
    }

    public Location getLocationByName(String name) {
        return locationRepository.findByName(name);
    }

}
