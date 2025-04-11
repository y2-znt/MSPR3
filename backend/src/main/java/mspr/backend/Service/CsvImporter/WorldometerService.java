package mspr.backend.Service.CsvImporter;

import mspr.backend.DTO.WorldometerDto;
import mspr.backend.Mapper.WorldometerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class WorldometerService {

    @Autowired
    private WorldometerMapper mapper;

    public void importData() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/worldometer_data.csv");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine(); // ignore header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // On utilise une méthode de split plus prudente ici car certaines colonnes sont entre guillemets
                // Par exemple, le titre "Serious,Critical" ou des nombres "1,234" pourraient poser problème.
                // Pour simplifier, on peut remplacer les occurrences de "\",\"" (guillemets-virgule-guillemets) temporairement ou utiliser un split régex.
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                // La regex ci-dessus sépare sur les virgules qui ne sont pas à l'intérieur de guillemets.
                // Ensuite on enlève les guillemets parasites:
                for(int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].replace("\"", "").trim();
                }
                // Champs attendus après nettoyage:
                // 0: Country/Region, 1: Continent, 2: Population, 3: TotalCases, 4: NewCases, 5: TotalDeaths,
                // 6: NewDeaths, 7: TotalRecovered, 8: NewRecovered, 9: ActiveCases, ... (on ignore le reste)
                String countryName = fields[0];
                String continent = fields[1];
                int population = fields[2].isEmpty() ? 0 : Integer.parseInt(fields[2]);
                int totalCases = fields[3].isEmpty() ? 0 : Integer.parseInt(fields[3]);
                int totalDeaths = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5]);
                int totalRecovered = fields[7].isEmpty() ? 0 : Integer.parseInt(fields[7]);
                int activeCases = fields[9].isEmpty() ? 0 : Integer.parseInt(fields[9]);
                WorldometerDto dto = new WorldometerDto(countryName, continent, population, totalCases, totalDeaths, totalRecovered, activeCases);
                mapper.dtoToEntity(dto);
                // Le mapper WorldometerMapper sauvegarde directement le Country mis à jour.
            }
        }
    }
}
