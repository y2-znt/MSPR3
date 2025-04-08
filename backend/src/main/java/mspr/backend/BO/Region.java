package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="region")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    private Set<Locations> locations = new HashSet<>();

    private String name;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    public Region() {}

    public Region(Set<Locations> locations, String name, Country country) {
        this.locations = locations;
        this.name = name;
        this.country = country;
    }

    public Set<Locations> getLocations() {
        return locations;
    }

    public void setLocations(Set<Locations> locations) {
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
