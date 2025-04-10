package mspr.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mspr.backend.BO.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    Location findByName(String name);
}
