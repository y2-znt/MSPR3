package mspr.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mspr.backend.BO.DiseaseCase;

@Repository
public interface DiseaseCaseRepository extends JpaRepository<DiseaseCase, Integer> {
    DiseaseCase findByName(String name);
}
