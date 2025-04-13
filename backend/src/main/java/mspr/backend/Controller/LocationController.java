package mspr.backend.Controller;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import mspr.backend.BO.Location;
import mspr.backend.Service.LocationService;

@RestController
@RequestMapping("/api/locations")

public class LocationController {
    @Autowired
    private LocationService locationService;

    @GetMapping
    public Page<Location> getAllLocations(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return locationService.getAllLocations(page, size);
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
