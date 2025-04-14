package mspr.backend.BO;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "Disease")
public class Disease {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "disease", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<DiseaseCase> diseaseCases = new HashSet<>();

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    public Disease() {}

    public Disease(String description, String name, Set<DiseaseCase> diseaseCases) {
        this.description = description;
        this.name = name;
        this.diseaseCases = diseaseCases;
    }

    public Set<DiseaseCase> getDiseaseCases() {
        return diseaseCases;
    }

    public void setDiseaseCases(Set<DiseaseCase> diseaseCases) {
        this.diseaseCases = diseaseCases;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Disease{" +
                "id=" + id +
                ", diseaseCases=" + diseaseCases +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

