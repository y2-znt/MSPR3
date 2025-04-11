package mspr.backend.Service.CsvImporter;

import mspr.backend.BO.DiseaseCase;
import mspr.backend.DTO.FullGroupedDto;
import mspr.backend.Mapper.FullGroupedMapper;
import mspr.backend.Repository.DiseaseCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Service
public class FullGroupedService {

    @Autowired
    private FullGroupedMapper mapper;
    @Autowired private DiseaseCaseRepository diseaseCaseRepository;

    public void importData() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/full_grouped.csv");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine(); // ignorer l'en-tête
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split(",");
                // Champ 0: Date, 1: Country/Region, 2: Confirmed, 3: Deaths, 4: Recovered, 5: Active,
                // 6: New cases, 7: New deaths, 8: New recovered, 9: WHO Region.
                LocalDate date = LocalDate.parse(fields[0]);
                String country = fields[1].trim();
                int confirmed = Integer.parseInt(fields[2]);
                int deaths = Integer.parseInt(fields[3]);
                int recovered = Integer.parseInt(fields[4]);
                int active = Integer.parseInt(fields[5]);
                // On ignore fields[6], [7], [8] (nouvelles cases) et [9] (WHO region).
                FullGroupedDto dto = new FullGroupedDto(date, country, confirmed, deaths, recovered, active);
                DiseaseCase diseaseCase = mapper.dtoToEntity(dto);
                diseaseCaseRepository.save(diseaseCase);
            }
        }
    }
}

