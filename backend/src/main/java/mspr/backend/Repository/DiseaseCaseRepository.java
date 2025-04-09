package mspr.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mspr.backend.BO.DiseaseCases;

@Repository
public interface DiseaseCaseRepository extends JpaRepository<DiseaseCases, Long> {
}
