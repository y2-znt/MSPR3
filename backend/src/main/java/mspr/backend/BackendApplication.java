package mspr.backend;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
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
