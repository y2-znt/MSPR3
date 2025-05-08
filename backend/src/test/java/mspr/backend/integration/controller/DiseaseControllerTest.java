package mspr.backend.integration.controller;

import java.util.List;

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

import mspr.backend.entity.Disease;
import mspr.backend.repository.DiseaseRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DiseaseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DiseaseRepository diseaseRepository;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        diseaseRepository.deleteAll();
        baseUrl = "http://localhost:" + port + "/api/diseases";
    }

    /**
     * Helper to create a test disease
     */
    private Disease createTestDisease(String name) {
        Disease disease = new Disease();
        disease.setName(name);
        return diseaseRepository.save(disease);
    }

    @Test
    @DisplayName("should create a disease when valid data is provided")
    public void testCreateDisease() {
        // Arrange
        Disease diseaseToCreate = new Disease();
        diseaseToCreate.setName("COVID-19");

        // Action
        ResponseEntity<Disease> response = restTemplate.postForEntity(
            baseUrl, diseaseToCreate, Disease.class);
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Disease createdDisease = response.getBody();
        assertNotNull(createdDisease);
        assertNotNull(createdDisease.getId());
        assertEquals("COVID-19", createdDisease.getName());
    }

    @Test
    @DisplayName("should retrieve list of diseases when called GET /api/diseases")
    public void testGetAllDiseases() {
        // Arrange
        createTestDisease("COVID-19");
        createTestDisease("Influenza");

        // Action
        ResponseEntity<List<Disease>> response = restTemplate.exchange(
            baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<Disease>>() {});
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Disease> diseases = response.getBody();
        assertNotNull(diseases);
        assertEquals(2, diseases.size());
    }

    @Test
    @DisplayName("should retrieve a disease by ID when called GET /api/diseases/{id}")
    public void testGetDiseaseById() {
        // Arrange
        Disease disease = createTestDisease("COVID-19");

        // Action
        ResponseEntity<Disease> response = restTemplate.getForEntity(
            baseUrl + "/" + disease.getId(), Disease.class);
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Disease retrievedDisease = response.getBody();
        assertNotNull(retrievedDisease);
        assertEquals("COVID-19", retrievedDisease.getName());
    }

    @Test
    @DisplayName("should update a disease when called PUT /api/diseases/{id}")
    public void testUpdateDisease() {
        // Arrange
        Disease disease = createTestDisease("COVID-19");
        disease.setName("COVID-19 Updated");

        // Action
        HttpEntity<Disease> requestEntity = new HttpEntity<>(disease);
        ResponseEntity<Disease> response = restTemplate.exchange(
            baseUrl + "/" + disease.getId(), 
            HttpMethod.PUT, 
            requestEntity, 
            Disease.class);
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Disease updatedDisease = response.getBody();
        assertNotNull(updatedDisease);
        assertEquals("COVID-19 Updated", updatedDisease.getName());
    }

    @Test
    @DisplayName("should delete a disease when called DELETE /api/diseases/{id}")
    public void testDeleteDisease() {
        // Arrange
        Disease disease = createTestDisease("COVID-19");
        Integer diseaseId = disease.getId();

        // Action
        restTemplate.delete(baseUrl + "/" + diseaseId);

        // Assertion
        ResponseEntity<Disease> response = restTemplate.getForEntity(
            baseUrl + "/" + diseaseId, Disease.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("should return 404 when trying to retrieve a non-existent disease")
    public void testDiseaseNotFound() {
        // Action
        ResponseEntity<Disease> response = restTemplate.getForEntity(
            baseUrl + "/9999", Disease.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
}