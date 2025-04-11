package mspr.backend.DTO;

import java.time.LocalDate;

public class CovidCompleteDto {
    private String provinceState;   // Province ou État (peut être vide si non applicable)
    private String countryRegion;   // Pays
    private Double lat;             // Latitude
    private Double lon;             // Longitude
    private LocalDate date;         // Date des données
    private int confirmed;          // Nombre confirmé
    private int deaths;             // Nombre de décès
    private int recovered;          // Nombre guéris
    private int active;             // Nombre actifs


    public CovidCompleteDto() {
    }

    public CovidCompleteDto(String provinceState, String countryRegion, Double lat, Double lon, LocalDate date, int confirmed, int deaths, int recovered, int active) {
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
