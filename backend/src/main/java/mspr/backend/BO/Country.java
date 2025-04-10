package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Country")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private Set<Region> regions = new HashSet<>();

    @Column(name = "continent")
    @Enumerated(EnumType.STRING)
    private ContinentEnum continent;

    @Column(name = "who_region")
    @Enumerated(EnumType.STRING)
    private WHORegionEnum whoRegion;

    @Column(name = "population")
    private Integer population;

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


}


