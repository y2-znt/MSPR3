package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "diseases")
public class Diseases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "diseases", cascade = CascadeType.ALL )
    private Set<DiseasesCases> diseasesCases = new HashSet<>();

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    public Diseases() {}

    public Diseases(String description, String name, Set<DiseasesCases> diseasesCases) {
        this.description = description;
        this.name = name;
        this.diseasesCases = diseasesCases;
    }

    public Set<DiseasesCases> getDiseasesCases() {
        return diseasesCases;
    }

    public void setDiseasesCases(Set<DiseasesCases> diseasesCases) {
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
