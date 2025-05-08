package mspr.backend.integration;

import mspr.backend.entity.Country;
import mspr.backend.repository.CountryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, 
properties = {"spring.test.mockmvc.timeout=3600000","server.port=8081", "spring.h2.console.enabled=true"})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class CountryControllerIntegrationTest {

    // Configuration interne pour désactiver les CommandLineRunner
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public CommandLineRunner noOpCommandLineRunner() {
            return args -> {
                System.out.println("\n========================================");
                System.out.println("   CommandLineRunner désactivé en mode test");
                System.out.println("   Aucune donnée ne sera chargée automatiquement");
                System.out.println("========================================\n");
            };
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CountryRepository countryRepository;

    private String getRootUrl() {
        return "http://localhost:" + port + "/api/countries";
    }

    @BeforeEach
    public void setup() {
        // Nettoyer la base de données avant chaque test
        countryRepository.deleteAll();
    }

    @Test
    public void testServerIsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);
        System.out.println("Server health response: " + response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("\n ✅ TEST: " + new Object(){}.getClass().getEnclosingMethod().getName() + " - RÉUSSI ✅\n");
    }


    @Test
    public void testCreateCountry() {
        System.out.println("\n----- DÉBUT TEST CREATE COUNTRY -----");
        
        // Créer un pays
        Country country = new Country();
        country.setName("Disneyland");
        System.out.println("Création du pays: " + country.getName());

        ResponseEntity<Country> postResponse = restTemplate.postForEntity(
                getRootUrl(), country, Country.class);
        
        System.out.println("Réponse HTTP: " + postResponse.getStatusCode());
        System.out.println("Corps de la réponse: " + postResponse.getBody());

        // Vérifier la réponse
        assertEquals(HttpStatus.OK, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        assertNotNull(postResponse.getBody().getId());
        assertEquals("Disneyland", postResponse.getBody().getName());
        
        System.out.println("ID du pays créé: " + postResponse.getBody().getId());
        System.out.println("----- FIN TEST CREATE COUNTRY -----\n");
        System.out.println("\n ✅ TEST: " + new Object(){}.getClass().getEnclosingMethod().getName() + " - RÉUSSI ✅\n");
    }

    @Test
    public void testGetAllCountries() {
        // Créer quelques pays dans la base de données
        Country Disneyland = new Country();
        Disneyland.setName("Disneyland");
        countryRepository.save(Disneyland);

        Country germany = new Country();
        germany.setName("Germany");
        countryRepository.save(germany);

        // Récupérer tous les pays
        ResponseEntity<Country[]> response = restTemplate.getForEntity(
                getRootUrl(), Country[].class);

        // Vérifier la réponse
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Country[] countries = response.getBody();
        assertNotNull(countries);
        assertEquals(2, countries.length);
        System.out.println("\n ✅ TEST: " + new Object(){}.getClass().getEnclosingMethod().getName() + " - RÉUSSI ✅\n");
    }

    @Test
    public void testGetCountryById() {
        // Créer un pays dans la base de données
        Country country = new Country();
        country.setName("Disneyland");
        country = countryRepository.save(country);

        // Récupérer le pays par son ID
        ResponseEntity<Country> response = restTemplate.getForEntity(
                getRootUrl() + "/" + country.getId(), Country.class);

        // Vérifier la réponse
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Disneyland", response.getBody().getName());
        System.out.println("\n ✅ TEST: " + new Object(){}.getClass().getEnclosingMethod().getName() + " - RÉUSSI ✅\n");
    }

    @Test
    public void testUpdateCountry() {
        // Créer un pays dans la base de données
        Country country = new Country();
        country.setName("Disneyland");
        country = countryRepository.save(country);

        // Modifier le pays
        country.setName("Disneyland Updated");
        
        HttpEntity<Country> requestEntity = new HttpEntity<>(country);
        ResponseEntity<Country> response = restTemplate.exchange(
                getRootUrl() + "/" + country.getId(), 
                HttpMethod.PUT, 
                requestEntity, 
                Country.class);

        // Vérifier la réponse
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Disneyland Updated", response.getBody().getName());
        System.out.println("\n ✅ TEST: " + new Object(){}.getClass().getEnclosingMethod().getName() + " - RÉUSSI ✅\n");
    }

    @Test
    public void testDeleteCountry() {
        // Créer un pays dans la base de données
        Country country = new Country();
        country.setName("Disneyland");
        country = countryRepository.save(country);

        // Supprimer le pays
        restTemplate.delete(getRootUrl() + "/" + country.getId());

        // Vérifier que le pays a été supprimé
        ResponseEntity<Country> response = restTemplate.getForEntity(
                getRootUrl() + "/" + country.getId(), Country.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        System.out.println("\n ✅ TEST: " + new Object(){}.getClass().getEnclosingMethod().getName() + " - RÉUSSI ✅\n");
    }

    @Test
    public void testCountryNotFound() {
        // Essayer de récupérer un pays qui n'existe pas
        ResponseEntity<Country> response = restTemplate.getForEntity(
                getRootUrl() + "/9999", Country.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        System.out.println("\n ✅ TEST: " + new Object(){}.getClass().getEnclosingMethod().getName() + " - RÉUSSI ✅\n");
    }

    @Test
    public void testCreateAndViewDisneyland() {
        // Créer Disneyland
        Country disneyland = new Country();
        disneyland.setName("Disneyland");
        countryRepository.save(disneyland);
        
        // Afficher des informations utiles
        System.out.println("\n===== DONNÉES DE TEST DISPONIBLES =====");
        System.out.println("Accédez à la console H2: http://localhost:" + port + "/h2");
        System.out.println("URL de connexion: jdbc:h2:mem:testdb");
        System.out.println("Utilisateur: sa");
        System.out.println("Mot de passe: password");
        System.out.println("Exécutez cette requête: SELECT * FROM COUNTRY");
        
        // Liste tous les pays
        System.out.println("\nPays dans la base de données:");
        countryRepository.findAll().forEach(c -> 
            System.out.println(" - ID: " + c.getId() + ", Nom: " + c.getName())
        );
        
        // Pause pour vous permettre d'accéder à la console H2
        try {
            System.out.println("\nAttente de 60 secondes pour vous permettre d'accéder à la console H2...");
            Thread.sleep(60000); // Attendre 60 secondes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//     Configuration :

// Utilise @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) pour démarrer un serveur web réel
// Injecte TestRestTemplate pour faire des requêtes HTTP réelles
// Utilise H2 en mémoire grâce à votre profil test
// Tests CRUD complets :

// testCreateCountry : Vérifie la création d'un pays
// testGetAllCountries : Vérifie la récupération de tous les pays
// testGetCountryById : Vérifie la récupération d'un pays par son ID
// testUpdateCountry : Vérifie la mise à jour d'un pays
// testDeleteCountry : Vérifie la suppression d'un pays
// Test d'erreur :

// testCountryNotFound : Vérifie que le contrôleur retourne bien 404 quand un pays n'existe pas
// Ce test vérifie l'ensemble de la chaîne, du contrôleur jusqu'à la base de données, en passant par les services. C'est un véritable test d'intégration qui vous permettra de vous assurer que votre API fonctionne correctement de bout en bout.
}