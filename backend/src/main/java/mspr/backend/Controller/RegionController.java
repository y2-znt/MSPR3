package mspr.backend.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mspr.backend.BO.Region;
import mspr.backend.Service.RegionService;

@RestController
@RequestMapping("/api/regions")

public class RegionController {
    @Autowired
    private RegionService regionService;

    @GetMapping
    public List<Region> getAllRegions() {
        return regionService.getRegionRepository().findAll();
    }
    @GetMapping("/{id}")
    public Optional<Region> getRegionById(@PathVariable Integer id) {
        return regionService.getRegionById(id);
    }
    @GetMapping("/name/{name}")
    public Region getRegionByName(@PathVariable String name) {
        return regionService.getRegionByName(name);
    }



}
