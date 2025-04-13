package mspr.backend.Service.CsvImporter;

import mspr.backend.BO.DiseaseCase;
import mspr.backend.DTO.FullGroupedDto;
import mspr.backend.Helpers.CleanerHelper;
import mspr.backend.Mapper.CovidCompleteMapper;
import mspr.backend.Repository.DiseaseCaseRepository;
import mspr.backend.DTO.CovidCompleteDto;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Service
public class CovidCompleteService {

    @Autowired
    private CovidCompleteMapper mapper;
    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;
    @Autowired
    private CleanerHelper cleanerHelper;

    public static final String FILE_NAME = "covid_19_clean_complete.csv";

    public void importData() throws Exception {

        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        if (Files.isRegularFile(path) && Files.exists(path)) {
            System.out.println("Le fichier existe et est un fichier régulier.");
        } else {
            System.out.println("Le fichier n'existe pas ou n'est pas un fichier régulier.");
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        HashMap<Integer, CovidCompleteDto> dtoMap = new HashMap<>();
        for (int l = 1; l < lines.size(); l++) {
            String line = lines.get(l);
            String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            String provinceStateName = fields[0];
            String countryRegionName = fields[1];
            Double lat = fields[2].isEmpty() ? 0 : Double.parseDouble(fields[2]);
            Double lon = fields[3].isEmpty() ? 0 : Double.parseDouble(fields[3]);
            LocalDate date = LocalDate.parse(fields[4], java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int confirmed = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5]);
            int deaths = fields[6].isEmpty() ? 0 : Integer.parseInt(fields[6]);
            int recovered = fields[7].isEmpty() ? 0 : Integer.parseInt(fields[7]);
            int active = fields[8].isEmpty() ? 0 : Integer.parseInt(fields[8]);
//            String whoRegion = fields[9]; // Not used
            CovidCompleteDto dto = new CovidCompleteDto(
                    provinceStateName,
                    cleanerHelper.cleanRegionName(countryRegionName),
                    lat,
                    lon,
                    date,
                    confirmed,
                    deaths,
                    recovered,
                    active
            );
            int hashKey = (provinceStateName + countryRegionName + lat + lon + date + confirmed + deaths + recovered + active).hashCode();
            dtoMap.put(hashKey, dto);
        }


        mapper.dtoToEntity(dtoMap);


    }

}