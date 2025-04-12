package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name="Region")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Location> locations = new HashSet<>();

    private String name;

    @ManyToOne
    @JoinColumn(name = "country_id")
    @JsonBackReference
    private Country country;

    public Region() {}

    public Region(Set<Location> locations, String name, Country country) {
        this.locations = locations;
        this.name = name;
        this.country = country;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Region{" +
                "id=" + id +
                ", locations=" + locations +
                ", name='" + name + '\'' +
                ", country=" + country +
                '}';
    }
}
