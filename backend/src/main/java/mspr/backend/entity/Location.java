package mspr.backend.entity;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(
        name="Location",
        indexes = { @Index(name = "idx_location_name", columnList = "name") }
)
@BatchSize(size = 50)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_seq")
    @SequenceGenerator(name = "location_seq", sequenceName = "location_seq", allocationSize = 50)
    private Integer id;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<DiseaseCase> diseasesCases = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "region_id")
    private Region region;

    private String name;

    public Location() {}

    public Location(Set<DiseaseCase> diseasesCases, String name, Region region) {
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

    public Set<DiseaseCase> getDiseasesCases() {
        return diseasesCases;
    }

    public void setDiseasesCases(Set<DiseaseCase> diseasesCases) {
        this.diseasesCases = diseasesCases;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
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
