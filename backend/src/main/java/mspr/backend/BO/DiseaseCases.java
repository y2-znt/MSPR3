package mspr.backend.BO;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="DiseaseCases")

public class DiseaseCases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "diseases_id")
    private Disease diseases;

    @ManyToOne
    @JoinColumn(name = "locations_id")
    private Location location;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "confirmed_cases")
    private Integer confirmedCases;

    @Column(name = "deaths")
    private Integer deaths;

    @Column(name = "recovered")
    private Integer recovered;

    public DiseaseCases(){}

    public DiseaseCases(Integer recovered, Integer deaths, Integer confirmedCases, LocalDate date, Location location, Disease diseases) {
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