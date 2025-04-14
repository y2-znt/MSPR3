package mspr.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mspr.backend.BO.DiseaseCase;

@Repository
public interface DiseaseCaseRepository extends JpaRepository<DiseaseCase, Integer> {
    // DiseaseCase findByName(String name);
    @Query("SELECT dc FROM DiseaseCase dc JOIN dc.disease d WHERE d.name = :name")
    DiseaseCase findByName(@Param("name") String name);
}
