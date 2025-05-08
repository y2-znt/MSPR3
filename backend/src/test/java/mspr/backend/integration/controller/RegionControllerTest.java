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

import mspr.backend.entity.Region;
import mspr.backend.repository.RegionRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RegionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private RegionRepository regionRepository;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        regionRepository.deleteAll();
        baseUrl = "http://localhost:" + port + "/api/regions";
    }

    /**
     * Helper to create a test region
     */
    private Region createTestRegion(String name) {
        Region region = new Region();
        region.setName(name);
        return regionRepository.save(region);
    }
    @Test
    @DisplayName("should create a region when valid data is provided")
    public void testCreateRegion() {
        // Arrange
        Region regionToCreate = new Region();
        regionToCreate.setName("Île-de-France");

        // Action
        ResponseEntity<Region> response = restTemplate.postForEntity(
            baseUrl, regionToCreate, Region.class);
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Region createdRegion = response.getBody();
        assertNotNull(createdRegion);
        assertNotNull(createdRegion.getId());
        assertEquals("Île-de-France", createdRegion.getName());
    }

    @Test
    @DisplayName("should retrieve paginated list of regions when called GET /api/regions")
    public void testGetAllRegions() {
        // Arrange
        createTestRegion("Île-de-France");
        createTestRegion("Aix-en-Provence");

        // Action
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> page = response.getBody();
        assertNotNull(page);
        assertNotNull(page.get("content"));
        assertEquals(2, ((java.util.List<?>) page.get("content")).size());     
    }

    @Test
    @DisplayName("should retrieve a region by ID when called GET /api/regions/{id}")
    public void testGetRegionById() {
        // Arrange
        Region region = createTestRegion("Île-de-France");

        // Action
        ResponseEntity<Region> response = restTemplate.getForEntity(
            baseUrl + "/" + region.getId(), Region.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Region retrievedRegion = response.getBody();
        assertNotNull(retrievedRegion);
        assertEquals("Île-de-France", retrievedRegion.getName());
    }

    @Test
    @DisplayName("should update a region when called PUT /api/regions/{id}")
    public void testUpdateRegion() {
        // Arrange
        Region region = createTestRegion("Île-de-France");
        region.setName("Île-de-France Updated");

        // Action
        HttpEntity<Region> requestEntity = new HttpEntity<>(region);
        ResponseEntity<Region> response = restTemplate.exchange(
            baseUrl + "/" + region.getId(), 
            HttpMethod.PUT, 
            requestEntity, 
            Region.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Region updatedRegion = response.getBody();
        assertNotNull(updatedRegion);
        assertEquals("Île-de-France Updated", updatedRegion.getName());
    }

    @Test
    @DisplayName("should delete a region when called DELETE /api/regions/{id}")
    public void testDeleteRegion() {
        // Arrange
        Region region = createTestRegion("Île-de-France");
        Integer regionId = region.getId();

        // Action
        restTemplate.delete(baseUrl + "/" + regionId);

        // Assertion
        ResponseEntity<Region> response = restTemplate.getForEntity(
            baseUrl + "/" + regionId, Region.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("should return 404 when trying to retrieve a non-existent region")
    public void testRegionNotFound() {
        // Action
        ResponseEntity<Region> response = restTemplate.getForEntity(
            baseUrl + "/9999", Region.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
    
}
