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
}
