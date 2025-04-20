package mspr.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mspr.backend.entity.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    Country findByName(String name);
}

