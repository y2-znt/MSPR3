package mspr.backend.Service;

import java.util.List;
import java.util.Optional;

import mspr.backend.BO.Country;
import mspr.backend.Repository.CountryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;

    public List<Country> getAllCountries() {
        return countryRepository.findAll();
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
