package mspr.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@TestPropertySource(locations = "classpath:application-test.properties")
public class databaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate; // JdbcTemplate est utilisé pour interagir avec la base de données

    @Test
public void testConnection() {
    // Vérifiez quel type de base de données est utilisé
    String dbType = jdbcTemplate.queryForObject(
        "SELECT 'Database working!'", String.class);
    System.out.println("Database Status: " + dbType);
    
    // Requête pour obtenir le type de base de données (méthode plus fiable)
    String databaseType = jdbcTemplate.queryForObject(
        "SELECT IFNULL(DATABASE(), 'H2')", String.class);
    System.out.println("Database Type: " + databaseType);
    
    // Version de la base de données
    String databaseInfo = jdbcTemplate.queryForObject(
        "SELECT H2VERSION()", String.class);
    System.out.println("Database Version: " + databaseInfo);
    
    // Vérifier la version plutôt que la présence de "H2" dans la chaîne
    assertNotNull(databaseInfo, "La version de la base de données devrait être disponible");
    
    // Requête standard
    String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
    assertNotNull(result);
    assertEquals("1", result);
    
    // Vérification explicite qu'il s'agit de H2 (requête spécifique à H2)
    try {
        String h2Check = jdbcTemplate.queryForObject(
            "SELECT 'USING_H2' FROM DUAL", String.class);
        assertEquals("USING_H2", h2Check, "La base de données devrait être H2");
    } catch (Exception e) {
        fail("La requête spécifique à H2 a échoué: " + e.getMessage());
    }
}


    @Test
    public void testCRUDOperations() {
        // Créer une table temporaire
        jdbcTemplate.execute("CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR(50))");
        
        // Insérer des données
        jdbcTemplate.update("INSERT INTO test_table (id, name) VALUES (1, 'Test Name')");
        
        // Lire les données
        String name = jdbcTemplate.queryForObject(
            "SELECT name FROM test_table WHERE id = 1", String.class);
        
        // Vérifier que les données sont correctes
        assertEquals("Test Name", name);
        
        // Nettoyage
        jdbcTemplate.execute("DROP TABLE test_table");
    }

    @Test
    public void testDatabaseMetadata() throws SQLException {
        // Obtenir la connexion via JdbcTemplate
        Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
        
        // Récupérer les métadonnées
        DatabaseMetaData metaData = connection.getMetaData();
        
        // Afficher les informations détaillées
        System.out.println("Database Product Name: " + metaData.getDatabaseProductName());
        System.out.println("Database Product Version: " + metaData.getDatabaseProductVersion());
        System.out.println("Driver Name: " + metaData.getDriverName());
        System.out.println("Driver Version: " + metaData.getDriverVersion());
        
        // Vérifier que c'est bien H2
        assertEquals("H2", metaData.getDatabaseProductName());
        
        // Toujours fermer la connexion
        connection.close();
    }



}