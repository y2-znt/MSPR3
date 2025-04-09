package mspr.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mspr.backend.BO.Disease;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Integer> {
    Disease findByName(String name);
}

