package mspr.backend.DTO;

import java.time.LocalDate;

public class UsaCountyDto {
    private String county;         // Nom du comté (Admin2 dans le CSV, ex: "Los Angeles")
    private String provinceState;  // État (Province_State dans le CSV, ex: "California")
    private String countryRegion;  // Pays (devrait être "US" pour toutes les lignes de ce fichier)
    private Double lat;            // Latitude du comté
    private Double lon;            // Longitude du comté
    private LocalDate date;        // Date
    private int confirmed;         // Confirmés cumulés
    private int deaths;            // Décès cumulés
    private int recovered;         // Guéris cumulés (pas fourni dans ce CSV, on mettra 0)
    private int active;            // Actifs cumulés (pas fourni, on peut mettre 0)


    public UsaCountyDto() {
    }

    public UsaCountyDto(String county, String provinceState, String countryRegion, Double lat, Double lon, LocalDate date, int confirmed, int deaths, int recovered, int active) {
        this.county = county;
        this.provinceState = provinceState;
        this.countryRegion = countryRegion;
        this.lat = lat;
        this.lon = lon;
        this.date = date;
        this.confirmed = confirmed;
        this.deaths = deaths;
        this.recovered = recovered;
        this.active = active;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getProvinceState() {
        return provinceState;
    }

    public void setProvinceState(String provinceState) {
        this.provinceState = provinceState;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

