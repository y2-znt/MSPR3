package mspr.backend.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mspr.backend.BO.Location;
import mspr.backend.Repository.LocationRepository;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Integer id) {
        return locationRepository.findById(id).orElse(null);
    }

    public Location getLocationByName(String name) {
        return locationRepository.findByName(name);
    }

}
