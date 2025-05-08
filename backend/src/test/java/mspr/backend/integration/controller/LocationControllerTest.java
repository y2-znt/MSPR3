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

import mspr.backend.entity.Location;
import mspr.backend.repository.LocationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LocationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private LocationRepository locationRepository;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        locationRepository.deleteAll();
        baseUrl = "http://localhost:" + port + "/api/locations";
    }

    /**
     * Helper to create a test location
     */
    private Location createTestLocation(String name) {
        Location location = new Location();
        location.setName(name);
        return locationRepository.save(location);
    }

    @Test
    @DisplayName("should create a location when valid data is provided")
    public void testCreateLocation() {
        // Arrange
        Location locationToCreate = new Location();
        locationToCreate.setName("Paris");

        // Action
        ResponseEntity<Location> response = restTemplate.postForEntity(
            baseUrl, locationToCreate, Location.class);
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Location createdLocation = response.getBody();
        assertNotNull(createdLocation); 
        assertNotNull(createdLocation.getId());
        assertEquals("Paris", createdLocation.getName());
    }

    @Test
    @DisplayName("should retrieve paginated list of locations when called GET /api/locations")
    public void testGetAllLocations() {
        // Arrange
        createTestLocation("Paris");
        createTestLocation("Marseille");
        
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
    @DisplayName("should retrieve a location by ID when called GET /api/locations/{id}")
    public void testGetLocationById() {
        // Arrange
        Location location = createTestLocation("Paris");

        // Action
        ResponseEntity<Location> response = restTemplate.getForEntity(
            baseUrl + "/" + location.getId(), Location.class);
        
        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Location retrievedLocation = response.getBody();
        assertNotNull(retrievedLocation);
        assertEquals("Paris", retrievedLocation.getName());
    }

    @Test
    @DisplayName("should update a location when called PUT /api/locations/{id}")
    public void testUpdateLocation() {
        // Arrange
        Location location = createTestLocation("Paris");
        location.setName("Paris Updated");

        // Action
        HttpEntity<Location> requestEntity = new HttpEntity<>(location);
        ResponseEntity<Location> response = restTemplate.exchange(
            baseUrl + "/" + location.getId(), 
            HttpMethod.PUT, 
            requestEntity, 
            Location.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Location updatedLocation = response.getBody();
        assertNotNull(updatedLocation);
        assertEquals("Paris Updated", updatedLocation.getName());
    }

    @Test
    @DisplayName("should delete a location when called DELETE /api/locations/{id}")
    public void testDeleteLocation() {
        // Arrange
        Location location = createTestLocation("Paris");
        Integer locationId = location.getId();

        // Action
        restTemplate.delete(baseUrl + "/" + locationId);

        // Assertion
        ResponseEntity<Location> response = restTemplate.getForEntity(
            baseUrl + "/" + locationId, Location.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }   

    @Test
    @DisplayName("should return 404 when trying to retrieve a non-existent location")
    public void testLocationNotFound() {
        // Action
        ResponseEntity<Location> response = restTemplate.getForEntity(
            baseUrl + "/9999", Location.class);

        // Assertion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
    

}
