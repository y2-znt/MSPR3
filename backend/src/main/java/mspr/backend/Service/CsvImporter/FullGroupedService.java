package mspr.backend.Service.CsvImporter;

import jakarta.transaction.Transactional;
import mspr.backend.BO.DiseaseCase;
import mspr.backend.DTO.FullGroupedDto;
import mspr.backend.Helpers.CleanerHelper;
import mspr.backend.Mapper.FullGroupedMapper;
import mspr.backend.Repository.DiseaseCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class FullGroupedService {

    public static final String FILE_NAME = "full_grouped.csv";

    @Autowired
    private FullGroupedMapper mapper;

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;

    @Autowired
    private CleanerHelper cleanerHelper;

    public void importData() throws Exception {
        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        if (Files.isRegularFile(path) && Files.exists(path)) {
            System.out.println("Le fichier existe et est un fichier régulier.");
        } else {
            System.out.println("Le fichier n'existe pas ou n'est pas un fichier régulier.");
            return;
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        HashMap<Integer, FullGroupedDto> dtoMap = new HashMap<>();

        // On ignore la première ligne (l'en-tête)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int l = 1; l < lines.size(); l++) {
            String line = lines.get(l);
            String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            LocalDate date = LocalDate.parse(fields[0].trim(), dateFormatter);
            String countryRegionName = cleanerHelper.cleanRegionName(cleanerHelper.cleanCountryName(fields[1].trim()));
            int confirmed = fields[2].isEmpty() ? 0 : Integer.parseInt(fields[2].trim());
            int deaths = fields[3].isEmpty() ? 0 : Integer.parseInt(fields[3].trim());
            int recovered = fields[4].isEmpty() ? 0 : Integer.parseInt(fields[4].trim());
            int active = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5].trim());
            String whoRegion = fields[9].trim();

            FullGroupedDto dto = new FullGroupedDto(
                    date,
                    countryRegionName,
                    confirmed,
                    deaths,
                    recovered,
                    active,
                    whoRegion
            );
            int hashKey = (date + countryRegionName + confirmed + deaths + recovered + active).hashCode();
            dtoMap.put(hashKey, dto);
        }

        // Conversion des DTO en DiseaseCase via le mapper
        List<DiseaseCase> diseaseCases = new ArrayList<>();
        for (FullGroupedDto dto : dtoMap.values()) {
            DiseaseCase diseaseCase = mapper.toEntity(dto);
            diseaseCases.add(diseaseCase);
        }

        // Insertion en batch de tous les DiseaseCase
        diseaseCaseRepository.saveAll(diseaseCases);
        System.out.println("Import Full Grouped terminé : " + diseaseCases.size() + " cas insérés.");
    }
}
