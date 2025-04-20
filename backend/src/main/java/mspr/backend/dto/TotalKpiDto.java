package mspr.backend.dto;

public class TotalKpiDto {
    private long totalCases;
    private long totalDeaths;
    private long totalRecovered;
    private double mortalityRate;
    private double recoveryRate;

    public TotalKpiDto(long totalCases, long totalDeaths, long totalRecovered) {
        this.totalCases = totalCases;
        this.totalDeaths = totalDeaths;
        this.totalRecovered = totalRecovered;
        this.mortalityRate = totalCases > 0 ? Math.round(((double) totalDeaths / totalCases * 100) * 100.0) / 100.0 : 0;
        this.recoveryRate = totalCases > 0 ? Math.round(((double) totalRecovered / totalCases * 100) * 100.0) / 100.0 : 0;
    }

    public long getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(long totalCases) {
        this.totalCases = totalCases;
    }

    public long getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(long totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public long getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(long totalRecovered) {
        this.totalRecovered = totalRecovered;
    }

    public double getMortalityRate() {
        return mortalityRate;
    }

    public void setMortalityRate(double mortalityRate) {
        this.mortalityRate = mortalityRate;
    }

    public double getRecoveryRate() {
        return recoveryRate;
    }

    public void setRecoveryRate(double recoveryRate) {
        this.recoveryRate = recoveryRate;
    }
}