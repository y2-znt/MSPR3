package mspr.backend.unit.controller;

import mspr.backend.controller.CountryController;
import mspr.backend.entity.Country;
import mspr.backend.service.CountryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FakeCountryService extends CountryService {
    @Override
    public Country createCountry(Country country) {
        country.setId(1);
        return country;
    }
}

public class CountryControllerTest {

    @Test
    void testCreateCountry() {
        // Arrange
        CountryService fakeService = new FakeCountryService();
        CountryController controller = new CountryController();
        // Injection manuelle du service
        controller = injectService(controller, fakeService);

        Country input = new Country();
        input.setName("Disneyland");

        // Act
        Country result = controller.createCountry(input);

        // Assert
        assertEquals("Disneyland", result.getName());
        assertEquals(1, result.getId());
    }

    // mInjection du service dans le contrôleur
    private CountryController injectService(CountryController controller, CountryService service) {
        try {
            var field = CountryController.class.getDeclaredField("countryService");
            field.setAccessible(true);
            field.set(controller, service);
            return controller;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
