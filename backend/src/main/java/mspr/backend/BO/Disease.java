package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "Disease")
public class Disease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "diseases", cascade = CascadeType.ALL )
    private Set<DiseaseCases> diseasesCases = new HashSet<>();

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    public Disease() {}

    public Disease(String description, String name, Set<DiseaseCases> diseasesCases) {
        this.description = description;
        this.name = name;
        this.diseasesCases = diseasesCases;
    }

    public Set<DiseaseCases> getDiseasesCases() {
        return diseasesCases;
    }

    public void setDiseasesCases(Set<DiseaseCases> diseasesCases) {
        this.diseasesCases = diseasesCases;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Diseases{" +
                "id=" + id +
                ", diseasesCases=" + diseasesCases +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
