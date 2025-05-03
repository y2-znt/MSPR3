package mspr.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CountryControllerTest {
    
    @Test
    @DisplayName("1 + 1 = 2")
    void additionTest() {
        assertEquals(2, add(1, 1), "Simple addition should work");
        System.out.println("Addition test passed");
    }
    
    // Simple calculator methods
    private int add(int a, int b) {
        return a + b;
    }
}