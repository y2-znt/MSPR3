package mspr.backend.Service.CsvImporter;

import mspr.backend.BO.DiseaseCase;
import mspr.backend.DTO.CovidCompleteDto;
import mspr.backend.Helpers.CleanerHelper;
import mspr.backend.Mapper.CovidCompleteMapper;
import mspr.backend.Repository.DiseaseCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

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
public class CovidCompleteService {

    public static final String FILE_NAME = "covid_19_clean_complete.csv";

    @Autowired
    private CovidCompleteMapper mapper;

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

        // Lecture du fichier CSV
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        HashMap<Integer, CovidCompleteDto> dtoMap = new HashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // On ignore la première ligne (l'en-tête)
        for (int l = 1; l < lines.size(); l++) {
            String line = lines.get(l);
            String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            // Extraction et nettoyage des champs
            String provinceStateName = fields[0].trim();
            String countryRegionName = fields[1].trim();
            Double lat = fields[2].isEmpty() ? 0.0 : Double.parseDouble(fields[2].trim());
            Double lon = fields[3].isEmpty() ? 0.0 : Double.parseDouble(fields[3].trim());
            LocalDate date = LocalDate.parse(fields[4].trim(), dateFormatter);
            int confirmed = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5].trim());
            int deaths = fields[6].isEmpty() ? 0 : Integer.parseInt(fields[6].trim());
            int recovered = fields[7].isEmpty() ? 0 : Integer.parseInt(fields[7].trim());
            int active = fields[8].isEmpty() ? 0 : Integer.parseInt(fields[8].trim());
            String whoRegion = fields[9].trim();

            CovidCompleteDto dto = new CovidCompleteDto(
                    provinceStateName,
                    cleanerHelper.cleanRegionName(countryRegionName),
                    lat,
                    lon,
                    date,
                    confirmed,
                    deaths,
                    recovered,
                    active,
                    whoRegion
            );

            int hashKey = (provinceStateName + countryRegionName + lat + lon + date + confirmed + deaths + recovered + active).hashCode();
            dtoMap.put(hashKey, dto);
        }

        // Conversion des DTO en DiseaseCase via le mapper
        List<DiseaseCase> diseaseCases = new ArrayList<>();
        for (CovidCompleteDto dto : dtoMap.values()) {
            DiseaseCase diseaseCase = mapper.toEntity(dto);
            diseaseCases.add(diseaseCase);
        }

        // Insertion en batch de tous les DiseaseCase
        diseaseCaseRepository.saveAll(diseaseCases);
        System.out.println("Import CovidComplete terminé : " + diseaseCases.size() + " cas insérés.");
    }
}
