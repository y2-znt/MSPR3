package mspr.backend;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		try {
			// Try to load the .env file if it exists
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(entry -> {
				if (System.getProperty(entry.getKey()) == null) {
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
		} catch (Exception e) {
			System.out.println("Note: No .env file found or error loading it. Using system environment variables.");
		}
		
		SpringApplication.run(BackendApplication.class, args);
	}

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	public void checkDatabaseConnection() {
		try (Connection conn = dataSource.getConnection()) {
			System.out.println("✅ Connexion à la base de données réussie !");
		} catch (Exception e) {
			System.out.println("❌ Échec de la connexion à la base de données : " + e.getMessage());
		}
	}

	@RestController
	public static class HelloController {

		@GetMapping("/")
		public String helloWorld() {
			return "Hello world";
		}
	}
}
