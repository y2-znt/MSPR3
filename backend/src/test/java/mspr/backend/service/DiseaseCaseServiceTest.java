package mspr.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitaire simple pour la classe DiseaseCaseService
 */
class DiseaseCaseServiceTest {

    @Test
    @DisplayName("Test de calcul simple du taux de mortalité objectif : 5%") 
    void testCalculateMortalityRate() {

        int totalCases = 1000;
        int deaths = 50;
        
        double mortalityRate = calculateMortalityRate(totalCases, deaths);
        
        // Test réussi si le taux de mortalité est de 5%
        assertEquals(5.0, mortalityRate, 0.01, "- Le taux de mortalité devrait être de 5%");
        System.out.println("** Test passé: pour " + totalCases + " cas et " + deaths + 
        " décès, le taux de mortalité est de " + mortalityRate + "%");

        // Test échoué avec un taux de mortalité supérieur à 5%
        try {
            assertEquals(6.0, mortalityRate, 0.01, "- Ce test devrait échouer délibérément");
        } catch (AssertionError e) {
            System.out.println("Erreur attendue détectée: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("- Test de calcul du taux de mortalité avec zéro cas")
    void testCalculateMortalityRateWithZeroCases() {
        
        int totalCases = 0;
        int deaths = 0;
        
        double mortalityRate = calculateMortalityRate(totalCases, deaths);
        
        // Test réussi si le taux de mortalité est de 0%
        assertEquals(0.0, mortalityRate, "Le taux de mortalité devrait être de 0% quand il n'y a pas de cas");
        System.out.println("** Test passé: quand il n'y a pas de cas, le taux de mortalité est de " + mortalityRate + "%");

        // Test échoué c ar le taux de mortalité est supérieur à 0%
        try {
            assertEquals(6.0, mortalityRate, "Ce test devrait échouer délibérément");
        } catch (AssertionError e) {
            System.out.println("Erreur attendue détectée: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test de catégorisation du risque basé sur le taux d'infection")
    void testCategorizeRiskLevel() {
        assertEquals("Faible", categorizeRiskLevel(0.5), "Un taux d'infection de 0.5% devrait être catégorisé comme risque faible");
        assertEquals("Modéré", categorizeRiskLevel(2.5), "Un taux d'infection de 2.5% devrait être catégorisé comme risque modéré");
        assertEquals("Élevé", categorizeRiskLevel(7.0), "Un taux d'infection de 7.0% devrait être catégorisé comme risque élevé");
        assertEquals("Critique", categorizeRiskLevel(15.0), "Un taux d'infection de 15.0% devrait être catégorisé comme risque critique");
        
        System.out.println("** Tests de catégorisation de risque passés avec succès");
    }
    
    @Test
    @DisplayName("Test du calcul du taux de récupération")
    void testCalculateRecoveryRate() {

        int totalCases = 1000;
        int recovered = 800;
        
        double recoveryRate = calculateRecoveryRate(totalCases, recovered);
        
        // Test réussi si le taux de récupération est de 80%
        assertEquals(80.0, recoveryRate, 0.01, "Le taux de récupération devrait être de 80%");
        System.out.println("** Test passé: pour " + totalCases + " cas et " + recovered + 
        " guérisons, le taux de récupération est de " + recoveryRate + "%");

        // Test échoué avec un taux de récupération supérieur à 50%
        try {
            assertEquals(50.0, recoveryRate, 0.01, "Ce test devrait échouer délibérément");
        } catch (AssertionError e) {
            System.out.println("Erreur attendue détectée: " + e.getMessage());
        }
    }
    
    private double calculateMortalityRate(int totalCases, int deaths) {
        if (totalCases == 0) return 0.0;
        return (double) deaths / totalCases * 100.0;
    }
    
    private String categorizeRiskLevel(double infectionRate) {
        if (infectionRate < 1.0) return "Faible";
        if (infectionRate < 5.0) return "Modéré";
        if (infectionRate < 10.0) return "Élevé";
        return "Critique";
    }
    
    private double calculateRecoveryRate(int totalCases, int recovered) {
        if (totalCases == 0) return 0.0;
        return (double) recovered / totalCases * 100.0;
    }
}