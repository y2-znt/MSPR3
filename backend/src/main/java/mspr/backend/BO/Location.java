package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.BatchSize;


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
    private Long id;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<DiseaseCase> diseasesCases = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "region_id")
    @JsonBackReference
    private Region region;

    private String name;

    public Location() {}

    public Location(Set<DiseaseCase> diseasesCases, String name, Region region) {
        this.diseasesCases = diseasesCases;
        this.name = name;
        this.region = region;
    }

    public Long getId() {
        return id;
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
