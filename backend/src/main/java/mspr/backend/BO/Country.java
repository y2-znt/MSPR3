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

    private String name;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private Set<Region> regions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ContinentEnum continent;

    @Enumerated(EnumType.STRING)
    private WHORegionEnum whoRegion;

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

    private Integer totalTests;

    public enum ContinentEnum {
        AFRICA, ASIA, EUROPE, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA, ANTARCTICA
    }

    public enum WHORegionEnum {
        AFR, AMR, EMR, EUR, SEAR, WPR
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


