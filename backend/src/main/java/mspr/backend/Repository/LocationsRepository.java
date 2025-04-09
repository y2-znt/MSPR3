package mspr.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mspr.backend.BO.Locations;

@Repository
public interface LocationsRepository extends JpaRepository<Locations, Long> {
}
