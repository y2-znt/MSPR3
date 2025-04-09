package mspr.backend.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mspr.backend.BO.Location;
import mspr.backend.Service.LocationService;

@RestController
@RequestMapping("/api/locations")

public class LocationController {
    @Autowired
    private LocationService locationService;

    @GetMapping
    public List<Location> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{id}")
    public Location getLocationById(@PathVariable Integer id) {
        return locationService.getLocationById(id);
    }

    @GetMapping("/name/{name}")
    public Location getLocationByName(@PathVariable String name) {
        return locationService.getLocationByName(name);
    }
}
