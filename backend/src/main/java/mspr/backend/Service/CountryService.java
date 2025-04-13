package mspr.backend.Service;

import java.util.Optional;

import mspr.backend.BO.Country;
import mspr.backend.Repository.CountryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;

    public Page<Country> getAllCountries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return countryRepository.findAll(pageable);
    }

    public Optional<Country> getCountryById(Integer id) {
        return countryRepository.findById(id);
    }

    public Country getCountryByName(String name) {
        return countryRepository.findByName(name);
    }

    public Country saveCountry(Country country) {
        return countryRepository.save(country);
    }

}
