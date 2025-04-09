package mspr.backend.BO;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="DiseaseCase")

public class DiseaseCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "disease_id")
    private Disease diseases;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "confirmed_case")
    private Integer confirmedCases;

    @Column(name = "death")
    private Integer deaths;

    @Column(name = "recovered")
    private Integer recovered;

    public DiseaseCase(){}

    public DiseaseCase(Integer recovered, Integer deaths, Integer confirmedCases, LocalDate date, Location location, Disease diseases) {
        this.recovered = recovered;
        this.deaths = deaths;
        this.confirmedCases = confirmedCases;
        this.date = date;
        this.location = location;
        this.diseases = diseases;
    }

    public Disease getDiseases() {
        return diseases;
    }

    public void setDiseases(Disease diseases) {
        this.diseases = diseases;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
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