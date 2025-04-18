package mspr.backend.etl.dto;

import java.time.LocalDate;

public class FullGroupedDto {
    private LocalDate date;        // Date
    private String countryRegion;  // Pays
    private int confirmed;         // Nombre confirmé (cumulé)
    private int deaths;            // Nombre de décès (cumulé)
    private int recovered;         // Nombre guéris (cumulé)
    private int active;            // Nombre actifs (cumulé)
    private String whoRegion;
    // (On ignore les colonnes "New cases", "New deaths", "New recovered" dans ce DTO)


    public FullGroupedDto() {
    }

    public FullGroupedDto(LocalDate date, String countryRegion, int confirmed, int deaths, int recovered, int active, String whoRegion) {
        this.date = date;
        this.countryRegion = countryRegion;
        this.confirmed = confirmed;
        this.deaths = deaths;
        this.recovered = recovered;
        this.active = active;
        this.whoRegion = whoRegion;
    }

    public String getWhoRegion() {
        return whoRegion;
    }

    public void setWhoRegion(String whoRegion) {
        this.whoRegion = whoRegion;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(int confirmed) {
        this.confirmed = confirmed;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getRecovered() {
        return recovered;
    }

    public void setRecovered(int recovered) {
        this.recovered = recovered;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
}