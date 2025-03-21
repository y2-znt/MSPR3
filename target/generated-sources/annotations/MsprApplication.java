package mspr.mspr;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsprApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().directory("backend").load();
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		System.out.println("Application started");
		SpringApplication.run(MsprApplication.class, args);

	}

}
