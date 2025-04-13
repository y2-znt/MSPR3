package mspr.backend.Service.CsvImporter;

import jakarta.transaction.Transactional;
import mspr.backend.BO.DiseaseCase;
import mspr.backend.DTO.FullGroupedDto;
import mspr.backend.DTO.WorldometerDto;
import mspr.backend.Helpers.CleanerHelper;
import mspr.backend.Mapper.FullGroupedMapper;
import mspr.backend.Repository.DiseaseCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class FullGroupedService {

    @Autowired private FullGroupedMapper mapper;
    @Autowired private DiseaseCaseRepository diseaseCaseRepository;
    @Autowired private CleanerHelper cleanerHelper;

    public static final String FILE_NAME = "full_grouped.csv";

    public void importData() throws Exception {

        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        if(Files.isRegularFile(path) && Files.exists(path)) {
            System.out.println("Le fichier existe et est un fichier régulier.");
        } else {
            System.out.println("Le fichier n'existe pas ou n'est pas un fichier régulier.");
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        HashMap<Integer, FullGroupedDto> dtoMap = new HashMap<>();
        for (int l = 1; l < lines.size(); l++) {
            String line = lines.get(l);
            String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            LocalDate date = LocalDate.parse(fields[0], java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String countryRegionName = cleanerHelper.cleanRegionName(cleanerHelper.cleanCountryName(fields[1]));
            int confirmed = fields[2].isEmpty() ? 0 : Integer.parseInt(fields[2]);
            int deaths = fields[3].isEmpty() ? 0 : Integer.parseInt(fields[3]);
            int recovered = fields[4].isEmpty() ? 0 : Integer.parseInt(fields[4]);
            int active = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5]);
//            int newCases = fields[6].isEmpty() ? 0 : Integer.parseInt(fields[6]);
//            int newDeaths = fields[7].isEmpty() ? 0 : Integer.parseInt(fields[7]);
//            int newRecovered = fields[8].isEmpty() ? 0 : Integer.parseInt(fields[8]);
            String whoRegion = fields[9];


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


        mapper.dtoToEntity(dtoMap);

    }
}

