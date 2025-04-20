package mspr.backend.controller;

import mspr.backend.entity.Country;
import mspr.backend.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @GetMapping
    public Page<Country> getAllCountries(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return countryService.getAllCountries(page, size);
    }

    @GetMapping("/{id}")
    public Country getCountryById(@PathVariable Integer id) {
        return countryService.getCountryById(id).orElse(null);
    }

    @PostMapping
    public Country createCountry(@RequestBody Country country) {
        return countryService.createCountry(country);
    }

    @PutMapping("/{id}")
    public Country updateCountry(@PathVariable Integer id, @RequestBody Country country) {
        return countryService.updateCountry(id, country);
    }

    @DeleteMapping("/{id}")
    public void deleteCountry(@PathVariable Integer id) {
        countryService.deleteCountry(id);
    }
}
