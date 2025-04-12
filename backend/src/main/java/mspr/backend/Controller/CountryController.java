package mspr.backend.Controller;


import mspr.backend.BO.Country;
import mspr.backend.Service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    public Optional<Country> getCountryById(@PathVariable Integer id) {
        return countryService.getCountryById(id);
    }


    @GetMapping("/name/{name}")
    public Optional<Country> getCountryByName(@PathVariable String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        // Validate the name format if necessary (e.g., regex for valid country names)
        if (!name.matches("^[a-zA-Z\\s]+$")) {
            return Optional.empty();
        }

        return countryService.getCountryByName(name);
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
