package mspr.backend.service;



import org.springframework.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mspr.backend.entity.Location;
import mspr.backend.repository.LocationRepository;

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

    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    public Location updateLocation(Integer id, Location location) {
        if (locationRepository.existsById(id)) {
            location.setId(id);
            return locationRepository.save(location);
        } else {
            return null;
        }
    }

    public void deleteLocation(Integer id) {
        locationRepository.deleteById(id);
    }


    


}
