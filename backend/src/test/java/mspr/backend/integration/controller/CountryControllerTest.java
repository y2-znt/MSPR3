package mspr.backend.integration.controller;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import mspr.backend.entity.Country;
import mspr.backend.repository.CountryRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, 
properties = {"spring.test.mockmvc.timeout=3600000","server.port=8081", "spring.h2.console.enabled=true"})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class CountryControllerTest {
    
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
        countryRepository.deleteAll();
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
        Country disneyland = new Country();
        disneyland.setName("Disneyland");
        countryRepository.save(disneyland);

        Country germany = new Country();
        germany.setName("Germany");
        countryRepository.save(germany);

        // Récupérer tous les pays (l'API retourne une page, pas un tableau)
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                getRootUrl(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Vérifier la réponse
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> page = response.getBody();
        assertNotNull(page.get("content"));
        assertEquals(2, ((java.util.List) page.get("content")).size());
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

        Integer countryId = country.getId();

        // Supprimer le pays
        restTemplate.delete(getRootUrl() + "/" + countryId);

        // Vérifier que le pays a été supprimé (l'API retourne null, pas 404)
        ResponseEntity<Country> response = restTemplate.getForEntity(
                getRootUrl() + "/" + countryId, Country.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        System.out.println("\n ✅ TEST: " + new Object(){}.getClass().getEnclosingMethod().getName() + " - RÉUSSI ✅\n");
    }

    @Test
    public void testCountryNotFound() {
        // Essayer de récupérer un pays qui n'existe pas (l'API retourne null, pas 404)
        ResponseEntity<Country> response = restTemplate.getForEntity(
                getRootUrl() + "/9999", Country.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
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
    }

}
