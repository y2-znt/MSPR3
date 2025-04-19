package mspr.backend.Repository;

import java.util.List;

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

    @Query("SELECT COALESCE(SUM(dc.deaths), 0) FROM DiseaseCase dc")
    long sumDeaths();

    @Query("SELECT COALESCE(SUM(dc.recovered), 0) FROM DiseaseCase dc")
    long sumRecovered();

    @Query("SELECT COALESCE(SUM(dc.confirmedCases), 0) FROM DiseaseCase dc")
    long sumCases();

    @Query("SELECT dc.date, SUM(dc.confirmedCases), SUM(dc.deaths), SUM(dc.recovered) " +
            "FROM DiseaseCase dc GROUP BY dc.date ORDER BY dc.date")
    List<Object[]> getAggregatedCasesByDate();

}
