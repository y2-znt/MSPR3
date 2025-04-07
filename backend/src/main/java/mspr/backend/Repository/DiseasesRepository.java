package mspr.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mspr.backend.BO.Diseases;

@Repository
public interface DiseasesRepository extends JpaRepository<Diseases, Long> {
}

