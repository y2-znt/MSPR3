package mspr.backend.BO;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="DiseasesCases")

public class DiseasesCases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "diseases_id")
    private Diseases diseases;

    @ManyToOne
    @JoinColumn(name = "locations_id")
    private Locations location;

    private LocalDate date;
    private Integer confirmedCases;
    private Integer deaths;
    private Integer recovered;

    public DiseasesCases(){}

    public DiseasesCases(Integer recovered, Integer deaths, Integer confirmedCases, LocalDate date, Locations location, Diseases diseases) {
        this.recovered = recovered;
        this.deaths = deaths;
        this.confirmedCases = confirmedCases;
        this.date = date;
        this.location = location;
        this.diseases = diseases;
    }

    public Diseases getDiseases() {
        return diseases;
    }

    public void setDiseases(Diseases diseases) {
        this.diseases = diseases;
    }

    public Locations getLocation() {
        return location;
    }

    public void setLocation(Locations location) {
        this.location = location;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getConfirmedCases() {
        return confirmedCases;
    }

    public void setConfirmedCases(Integer confirmedCases) {
        this.confirmedCases = confirmedCases;
    }

    public Integer getDeaths() {
        return deaths;
    }

    public void setDeaths(Integer deaths) {
        this.deaths = deaths;
    }

    public Integer getRecovered() {
        return recovered;
    }

    public void setRecovered(Integer recovered) {
        this.recovered = recovered;
    }

    @Override
    public String toString() {
        return "DiseasesCases{" +
                "id=" + id +
                ", diseases=" + diseases +
                ", location=" + location +
                ", date=" + date +
                ", confirmedCases=" + confirmedCases +
                ", deaths=" + deaths +
                ", recovered=" + recovered +
                '}';
    }
}