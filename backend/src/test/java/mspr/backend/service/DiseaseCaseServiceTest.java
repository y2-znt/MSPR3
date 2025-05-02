package mspr.backend.service;

import mspr.backend.entity.DiseaseCase;
import mspr.backend.entity.Disease;
import mspr.backend.entity.Country;
import mspr.backend.repository.DiseaseCaseRepository;
import mspr.backend.repository.DiseaseRepository;
import mspr.backend.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class DiseaseCaseServiceTest {

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;
    
    @Autowired
    private DiseaseRepository diseaseRepository;
    
    @Autowired
    private CountryRepository countryRepository;
    
    @Autowired
    private DiseaseCaseService diseaseCaseService;
    
    private DiseaseCase testCase;
    private Disease testDisease;
    private Country testCountry;
    
    @BeforeEach
    void setUp() {
        // Nettoyer les données existantes
        diseaseCaseRepository.deleteAll();
        
        // Créer et sauvegarder les entités de test
        testCountry = new Country();
        testCountry.setName("France");
        testCountry = countryRepository.save(testCountry);
        
        testDisease = new Disease();
        testDisease.setName("COVID-19");
        testDisease = diseaseRepository.save(testDisease);
        
        testCase = new DiseaseCase();
        testCase.setCountry("France");
        testCase.setDisease(testDisease);
        testCase.setDate(LocalDate.of(2023, 1, 1));
        testCase.setConfirmed(100);
        testCase.setDeaths(10);
        testCase.setRecovered(80);
        testCase = diseaseCaseRepository.save(testCase);
    }
    
    @Test
    @DisplayName("Doit récupérer les cas pour un pays et une maladie donnés")
    void testGetCasesByCountryAndDisease() {
        // Act
        List<DiseaseCase> cases = diseaseCaseService.getCasesByCountryAndDisease("France", "COVID-19");
        
        // Assert
        assertFalse(cases.isEmpty(), "La liste de cas ne devrait pas être vide");
        assertEquals(1, cases.size(), "Devrait récupérer un cas");
        assertEquals("France", cases.get(0).getCountry(), "Le pays devrait être France");
        assertEquals("COVID-19", cases.get(0).getDisease().getName(), "La maladie devrait être COVID-19");
    }
    
    @Test
    @DisplayName("Doit calculer correctement le taux de mortalité")
    void testCalculateMortalityRate() {
        // Act
        double mortalityRate = diseaseCaseService.calculateMortalityRate(testCase);
        
        // Assert
        assertEquals(0.1, mortalityRate, 0.001, "Le taux de mortalité devrait être de 10%");
    }
    
    @Test
    @DisplayName("Doit récupérer les statistiques globales par maladie")
    void testGetGlobalStatsByDisease() {
        // Arrange - Ajouter un autre cas pour la même maladie
        DiseaseCase case2 = new DiseaseCase();
        case2.setCountry("Germany");
        case2.setDisease(testDisease);
        case2.setDate(LocalDate.of(2023, 1, 1));
        case2.setConfirmed(200);
        case2.setDeaths(20);
        case2.setRecovered(160);
        diseaseCaseRepository.save(case2);
        
        // Act
        Map<String, Object> stats = diseaseCaseService.getGlobalStatsByDisease("COVID-19");
        
        // Assert
        assertEquals(300, stats.get("totalConfirmed"), "Le total des cas confirmés devrait être 300");
        assertEquals(30, stats.get("totalDeaths"), "Le total des décès devrait être 30");
        assertEquals(0.1, (double)stats.get("mortalityRate"), 0.001, "Le taux de mortalité devrait être 10%");
    }
    
    @Test
    @DisplayName("Doit retourner une liste vide pour un pays inexistant")
    void testGetCasesForNonExistentCountry() {
        // Act
        List<DiseaseCase> cases = diseaseCaseService.getCasesByCountryAndDisease("NonExistentCountry", "COVID-19");
        
        // Assert
        assertTrue(cases.isEmpty(), "La liste devrait être vide pour un pays inexistant");
    }
    
    @Test
    @DisplayName("Doit retourner une liste vide pour une maladie inexistante")
    void testGetCasesForNonExistentDisease() {
        // Act
        List<DiseaseCase> cases = diseaseCaseService.getCasesByCountryAndDisease("France", "NonExistentDisease");
        
        // Assert
        assertTrue(cases.isEmpty(), "La liste devrait être vide pour une maladie inexistante");
    }
    
    @Test
    @DisplayName("Doit réussir à créer un nouveau cas de maladie")
    void testCreateNewDiseaseCase() {
        // Arrange
        DiseaseCase newCase = new DiseaseCase();
        newCase.setCountry("Italy");
        newCase.setDisease(testDisease);
        newCase.setDate(LocalDate.of(2023, 1, 2));
        newCase.setConfirmed(150);
        newCase.setDeaths(15);
        newCase.setRecovered(120);
        
        // Act
        DiseaseCase savedCase = diseaseCaseService.saveDiseaseCase(newCase);
        
        // Assert
        assertNotNull(savedCase.getId(), "L'ID devrait être défini après la sauvegarde");
        assertEquals("Italy", savedCase.getCountry(), "Le pays devrait être Italy");
        assertEquals(testDisease.getId(), savedCase.getDisease().getId(), "La maladie devrait être COVID-19");
        
        // Vérifier que le cas est bien en base
        List<DiseaseCase> italyCases = diseaseCaseService.getCasesByCountryAndDisease("Italy", "COVID-19");
        assertEquals(1, italyCases.size(), "Devrait trouver un cas pour l'Italie");
    }
}