package mspr.backend.Service.CsvImporter;

import mspr.backend.BO.DiseaseCase;
import mspr.backend.Mapper.CovidCompleteMapper;
import mspr.backend.Repository.DiseaseCaseRepository;
import mspr.backend.DTO.CovidCompleteDto;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Service
public class CovidCompleteService {

    @Autowired
    private CovidCompleteMapper mapper;
    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;

    public void importData() throws Exception {
        // Ouvrir le fichier CSV depuis les ressources
        ClassPathResource resource = new ClassPathResource("data/covid_19_clean_complete.csv");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String header = br.readLine(); // lire et ignorer l'en-tête
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // ignorer les lignes vides éventuelles

                // Le fichier est séparé par des virgules, on split directement
                String[] fields = line.split(",");
                // Attention: Province/State peut être vide, ce qui donnerait fields[0] = "".

                // Extraire et convertir les valeurs
                String province = fields[0].trim();              // province ou état (vide si pas de province)
                String country = fields[1].trim();               // pays
                double lat = Double.parseDouble(fields[2]);      // latitude
                double lon = Double.parseDouble(fields[3]);      // longitude
                LocalDate date = LocalDate.parse(fields[4]);     // date au format YYYY-MM-DD (déjà dans ce format dans le CSV)
                int confirmed = Integer.parseInt(fields[5]);     // cas confirmés
                int deaths = Integer.parseInt(fields[6]);        // décès
                int recovered = Integer.parseInt(fields[7]);     // guérisons
                int active = Integer.parseInt(fields[8]);        // cas actifs

                // Construire le DTO
                CovidCompleteDto dto = new CovidCompleteDto(province, country, lat, lon, date, confirmed, deaths, recovered, active);

                // Mapper le DTO vers les entités (DiseaseCase relié à Location, etc.)
                DiseaseCase diseaseCase = mapper.dtoToEntity(dto);

                // Sauvegarder le cas en base
                diseaseCaseRepository.save(diseaseCase);
            }
        }
    }
}