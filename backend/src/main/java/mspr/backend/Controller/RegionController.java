package mspr.backend.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

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



}
