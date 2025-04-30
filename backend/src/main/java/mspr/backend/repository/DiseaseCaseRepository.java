package mspr.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mspr.backend.entity.DiseaseCase;

@Repository
public interface DiseaseCaseRepository extends JpaRepository<DiseaseCase, Integer> {

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

    @Query("SELECT dc.date, c.name, SUM(dc.confirmedCases), SUM(dc.deaths), SUM(dc.recovered), MAX(dc.id) " +
            "FROM DiseaseCase dc JOIN dc.location l JOIN l.region r JOIN r.country c " +
            "WHERE c.name IN :countries " +
            "GROUP BY dc.date, c.name ORDER BY dc.date, c.name")
    List<Object[]> getAggregatedCasesByDateAndCountries(
            @Param("countries") List<String> countries);

    @Query("SELECT dc.date, c.name, SUM(dc.confirmedCases), SUM(dc.deaths), SUM(dc.recovered), MAX(dc.id) " +
            "FROM DiseaseCase dc JOIN dc.location l JOIN l.region r JOIN r.country c " +
            "GROUP BY dc.date, c.name ORDER BY dc.date, c.name")
    List<Object[]> getAggregatedCasesByDateAllCountries();

    @Query("SELECT dc FROM DiseaseCase dc JOIN dc.location l WHERE l.name = :countryName ORDER BY dc.date DESC")
    List<DiseaseCase> findByLocationNameOrderByDateDesc(@Param("countryName") String countryName);
}
