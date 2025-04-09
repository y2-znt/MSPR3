package mspr.backend.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mspr.backend.BO.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}

