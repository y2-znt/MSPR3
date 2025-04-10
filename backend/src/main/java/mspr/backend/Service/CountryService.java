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

    public Optional<Country> getCountryByName(String name) {
        return countryRepository.findAll().stream()
                .filter(country -> country.getName().equalsIgnoreCase(name))
                .findFirst();
    }

}
