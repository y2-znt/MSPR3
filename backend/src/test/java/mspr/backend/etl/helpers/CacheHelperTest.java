package mspr.backend.etl.helpers;

import mspr.backend.BO.Country;
import mspr.backend.BO.Region;
import mspr.backend.BO.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CacheHelperTest {

    private CacheHelper cacheHelper;
    
    @BeforeEach
    public void setUp() {
        cacheHelper = new CacheHelper();
    }
    
    @Test
    public void testCountryCreation() {
        // Teste la création d'un pays et vérifie qu'une région standard est créée automatiquement
        Country country = cacheHelper.getOrCreateCountry("France");
        
        assertNotNull(country);
        assertEquals("France", country.getName());
        
        // Vérifie que la région standard a été créée automatiquement
        String regionKey = "France|France - region standard";
        assertTrue(cacheHelper.getRegions().containsKey(regionKey));
        Region standardRegion = cacheHelper.getRegions().get(regionKey);
        
        assertEquals("France - region standard", standardRegion.getName());
        assertEquals(country, standardRegion.getCountry());
        
        // Vérifie que la location standard a été créée automatiquement pour la région standard
        String locationKey = "France|France - region standard|France - region standard - location standard";
        assertTrue(cacheHelper.getLocations().containsKey(locationKey));
    }
    
    @Test
    public void testStandardNameReplacement() {
        // Teste que les noms "standard" sont correctement remplacés
        Country country = cacheHelper.getOrCreateCountry("Germany");
        Region region = cacheHelper.getOrCreateRegion(country, "standard");
        
        assertEquals("Germany - region standard", region.getName());
        
        // Teste la création d'une location standard
        Location location = cacheHelper.getOrCreateLocation(region, "standard");
        assertEquals("Germany - region standard - location standard", location.getName());
    }
    
    @Test
    public void testEmptyNameHandling() {
        // Teste que les noms vides sont correctement gérés
        Country country = cacheHelper.getOrCreateCountry("Italy");
        Region region = cacheHelper.getOrCreateRegionWithEmptyHandling(country, "");
        
        assertEquals("Italy - region standard", region.getName());
        
        // Teste la gestion des noms vides pour les locations
        Location location = cacheHelper.getOrCreateLocationWithEmptyHandling(region, null);
        assertEquals("Italy - region standard - location standard", location.getName());
    }
    
    @Test
    public void testCacheConsistency() {
        // Vérifie que les mêmes objets sont renvoyés quand on les demande plusieurs fois
        Country country = cacheHelper.getOrCreateCountry("Spain");
        Region region = cacheHelper.getOrCreateRegion(country, "Catalonia");
        Location location = cacheHelper.getOrCreateLocation(region, "Barcelona");
        
        // Demande les mêmes objets une deuxième fois
        Country country2 = cacheHelper.getOrCreateCountry("Spain");
        Region region2 = cacheHelper.getOrCreateRegion(country2, "Catalonia");
        Location location2 = cacheHelper.getOrCreateLocation(region2, "Barcelona");
        
        // Vérifie que ce sont les mêmes instances
        assertSame(country, country2);
        assertSame(region, region2);
        assertSame(location, location2);
    }
    
    @Test
    public void testNoDuplication() {
        // Vérifie qu'un pays n'est créé qu'une seule fois
        Country country1 = cacheHelper.getOrCreateCountry("Portugal");
        Country country2 = cacheHelper.getOrCreateCountry("Portugal");
        
        assertSame(country1, country2);
        assertEquals(1, cacheHelper.getCountries().size());
        
        // Vérifie qu'une région n'est créée qu'une seule fois
        Region region1 = cacheHelper.getOrCreateRegion(country1, "Lisbon");
        Region region2 = cacheHelper.getOrCreateRegion(country1, "Lisbon");
        
        assertSame(region1, region2);
        assertEquals(2, cacheHelper.getRegions().size()); // 2 car il y a aussi la région standard créée avec le pays
        
        // Vérifie qu'une location n'est créée qu'une seule fois
        Location location1 = cacheHelper.getOrCreateLocation(region1, "Belem");
        Location location2 = cacheHelper.getOrCreateLocation(region1, "Belem");
        
        assertSame(location1, location2);
    }
    
    @Test
    public void testUniqueLocationNamesGlobally() {
        // Crée deux pays différents
        Country usa = cacheHelper.getOrCreateCountry("USA");
        Country france = cacheHelper.getOrCreateCountry("France");
        
        // Crée deux régions différentes
        Region california = cacheHelper.getOrCreateRegion(usa, "California");
        Region paris = cacheHelper.getOrCreateRegion(france, "Paris");
        
        // Tente de créer une location avec le même nom dans deux régions différentes
        String locationName = "Washington";
        
        // Première location dans California
        Location washington1 = cacheHelper.getOrCreateLocation(california, locationName);
        assertEquals(locationName, washington1.getName());
        
        // Deuxième location dans Paris - le nom devrait être modifié pour éviter le doublon
        Location washington2 = cacheHelper.getOrCreateLocation(paris, locationName);
        
        // Le nom devrait être préfixé avec le nom de la région
        assertEquals("Paris - Washington", washington2.getName());
        
        // Vérifier que les deux locations sont différentes
        assertNotEquals(washington1, washington2);
        assertNotEquals(washington1.getName(), washington2.getName());
    }
} 