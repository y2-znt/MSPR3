package mspr.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mspr.backend.entity.Location;
import mspr.backend.service.LocationService;

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

    @PostMapping
    public Location createLocation(Location location) {
        return locationService.createLocation(location);
    }
    @PutMapping("/{id}")
    public Location updateLocation(@PathVariable Integer id, Location location) {
        return locationService.updateLocation(id, location);
    }

    @DeleteMapping("/{id}")
    public void deleteLocation(@PathVariable Integer id) {
        locationService.deleteLocation(id);
    }
}
