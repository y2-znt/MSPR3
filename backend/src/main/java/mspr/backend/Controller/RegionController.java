package mspr.backend.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mspr.backend.BO.Region;
import mspr.backend.Service.RegionService;

@RestController
@RequestMapping("/api/regions")

public class RegionController {
    @Autowired
    private RegionService regionService;

    @GetMapping
    public Page<Region> getAllRegions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return regionService.getAllRegions(page, size);
    }

    @GetMapping("/{id}")
    public Optional<Region> getRegionById(@PathVariable Integer id) {
        return regionService.getRegionById(id);
    }
    @GetMapping("/name/{name}")
    public Region getRegionByName(@PathVariable String name) {
        return regionService.getRegionByName(name);
    }

    @PostMapping
    public Region createRegion(Region region) {
        return regionService.getRegionRepository().save(region);
    }

    @PutMapping("/{id}")
    public Region updateRegion(@PathVariable Integer id, Region region) {
        return regionService.updateRegion(id, region);
    }

    @DeleteMapping("/{id}")
    public void deleteRegion(@PathVariable Integer id) {
        regionService.deleteRegion(id);
    }

}
