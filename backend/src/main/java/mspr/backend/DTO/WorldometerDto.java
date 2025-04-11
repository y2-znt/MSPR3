package mspr.backend.DTO;

import mspr.backend.BO.Country;

public class WorldometerDto {
    private String country;     // Pays
    private String continent;   // Continent
    private int population;     // Population du pays
    private int totalCases;     // Total des cas confirmés (cumulé)
    private int totalDeaths;    // Total des décès
    private int totalRecovered; // Total des guérisons
    private int activeCases;    // Cas actifs
    private String whoRegion;


    public WorldometerDto() {
    }

    public WorldometerDto(String country, String continent, int population, int totalCases, int totalDeaths, int totalRecovered, int activeCases, String whoRegion) {
        this.country = country;
        this.continent = continent;
        this.population = population;
        this.totalCases = totalCases;
        this.totalDeaths = totalDeaths;
        this.totalRecovered = totalRecovered;
        this.activeCases = activeCases;
        this.whoRegion = whoRegion;
    }

    public String getWhoRegion() {
        return whoRegion;
    }

    public void setWhoRegion(String whoRegion) {
        this.whoRegion = whoRegion;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public int getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(int totalRecovered) {
        this.totalRecovered = totalRecovered;
    }

    public int getActiveCases() {
        return activeCases;
    }

    public void setActiveCases(int activeCases) {
        this.activeCases = activeCases;
    }
}