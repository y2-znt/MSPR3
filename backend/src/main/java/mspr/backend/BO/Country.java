package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(
        name="Country",
        indexes = { @Index(name = "idx_country_name", columnList = "name") }
)
@BatchSize(size = 50)
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "country_seq")
    @SequenceGenerator(name = "country_seq", sequenceName = "country_seq", allocationSize = 50)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Region> regions = new HashSet<>();

    @Column(name = "continent")
    @Enumerated(EnumType.STRING)
    private ContinentEnum continent;

    @Column(name = "who_region")
    @Enumerated(EnumType.STRING)
    private WHORegionEnum whoRegion;

    @Column(name = "population")
    private Integer population;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Region> getRegions() {
        return regions;
    }

    public void setRegions(Set<Region> regions) {
        this.regions = regions;
    }

    public ContinentEnum getContinent() {
        return continent;
    }

    public void setContinent(ContinentEnum continent) {
        this.continent = continent;
    }

    public WHORegionEnum getWhoRegion() {
        return whoRegion;
    }

    public void setWhoRegion(WHORegionEnum whoRegion) {
        this.whoRegion = whoRegion;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public Integer getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Integer totalTests) {
        this.totalTests = totalTests;
    }

    @Column(name = "total_tests")
    private Integer totalTests;

    public enum ContinentEnum {
        AFRICA, ASIA, EUROPE, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA, ANTARCTICA
    }

    public enum WHORegionEnum {
        Americas, Africa, Western_Pacific, Eastern_Mediterranean, Europe, South_East_Asia
    }

    public Country() {}

    public Country(String name, ContinentEnum continent, WHORegionEnum whoRegion, Integer population, Integer totalTests) {
        this.name = name;
        this.continent = continent;
        this.whoRegion = whoRegion;
        this.population = population;
        this.totalTests = totalTests;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    


}


