package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name="locations")
public class Locations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private Set<DiseasesCases> diseasesCases = new HashSet<>();

    private String name;

    public Locations() {}

    public Locations(Set<DiseasesCases> diseasesCases, String name, Region region) {
        this.diseasesCases = diseasesCases;
        this.name = name;
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<DiseasesCases> getDiseasesCases() {
        return diseasesCases;
    }

    public void setDiseasesCases(Set<DiseasesCases> diseasesCases) {
        this.diseasesCases = diseasesCases;
    }

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;

    @Override
    public String toString() {
        return "Locations{" +
                "id=" + id +
                ", diseasesCases=" + diseasesCases +
                ", name='" + name + '\'' +
                ", region=" + region +
                '}';
    }
}
