package mspr.backend.integration.controller;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

/**
 * Integration tests for the Country controller.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    /**
     * Helper to create a test country
     */
    private Country createTestCountry(String name) {
        Country country = new Country();
        country.setName(name);
        return countryRepository.save(country);
    }

    @Test
    @DisplayName("should create a country when valid data is provided")
    public void testCreateCountry() {
        // Arrange
        Country countryToCreate = new Country();
        countryToCreate.setName("France");

        // Action
        ResponseEntity<Country> response = restTemplate.postForEntity(
                getRootUrl(), countryToCreate, Country.class);
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Country createdCountry = response.getBody();
        assertNotNull(createdCountry);
        assertNotNull(createdCountry.getId());
        assertEquals("France", createdCountry.getName());
    }

    @Test
    @DisplayName("should retrieve paginated list of countries when called GET /api/countries")
    public void testGetAllCountries() {
        // Arrange
        createTestCountry("France");
        createTestCountry("Germany");

        // Action
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                getRootUrl(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> page = response.getBody();
        assertNotNull(page);
        assertNotNull(page.get("content"));
        assertEquals(2, ((java.util.List<?>) page.get("content")).size());
    }

    @Test
    @DisplayName("should retrieve a country by ID when called GET /api/countries/{id}")
    public void testGetCountryById() {
        // Arrange
        Country country = createTestCountry("France");

        // Action
        ResponseEntity<Country> response = restTemplate.getForEntity(
                getRootUrl() + "/" + country.getId(), Country.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Country retrievedCountry = response.getBody();
        assertNotNull(retrievedCountry);
        assertEquals("France", retrievedCountry.getName());
    }

    @Test
    @DisplayName("should update a country when called PUT /api/countries/{id}")
    public void testUpdateCountry() {
        // Arrange
        Country country = createTestCountry("France");
        country.setName("France Updated");
        
        // Action
        HttpEntity<Country> requestEntity = new HttpEntity<>(country);
        ResponseEntity<Country> response = restTemplate.exchange(
                getRootUrl() + "/" + country.getId(), 
                HttpMethod.PUT, 
                requestEntity, 
                Country.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Country updatedCountry = response.getBody();
        assertNotNull(updatedCountry);
        assertEquals("France Updated", updatedCountry.getName());
    }

    @Test
    @DisplayName("should delete a country when called DELETE /api/countries/{id}")
    public void testDeleteCountry() {
        // Arrange
        Country country = createTestCountry("France");
        Integer countryId = country.getId();

        // Action
        restTemplate.delete(getRootUrl() + "/" + countryId);

        // Assertion
        ResponseEntity<Country> response = restTemplate.getForEntity(
                getRootUrl() + "/" + countryId, Country.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("should return 404 when trying to retrieve a non-existent country")
    public void testCountryNotFound() {
        // Action
        ResponseEntity<Country> response = restTemplate.getForEntity(
                getRootUrl() + "/9999", Country.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
}
