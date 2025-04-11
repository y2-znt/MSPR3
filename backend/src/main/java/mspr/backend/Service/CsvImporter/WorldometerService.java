package mspr.backend.Service.CsvImporter;

import mspr.backend.BO.Country;
import mspr.backend.DTO.WorldometerDto;
import mspr.backend.Mapper.WorldometerMapper;
import mspr.backend.Repository.CountryRepository;
import mspr.backend.Service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class WorldometerService {

    @Autowired
    private WorldometerMapper mapper;


    public static final String FILE_NAME = "worldometer_data.csv";

    public void importData() throws Exception {

        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        if(Files.isRegularFile(path) && Files.exists(path)) {
            System.out.println("Le fichier existe et est un fichier régulier.");
        } else {
            System.out.println("Le fichier n'existe pas ou n'est pas un fichier régulier.");
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        HashMap<Integer, WorldometerDto> dtoMap = new HashMap<>();
        for (int l = 1; l < lines.size(); l++) {
            String line = lines.get(l);

            String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            String countryName = fields[0];
            String continent = fields[1];
            int population = fields[2].isEmpty() ? 0 : Integer.parseInt(fields[2]);
            int totalCases = fields[3].isEmpty() ? 0 : Integer.parseInt(fields[3]);
            int totalDeaths = fields[5].isEmpty() ? 0 : Integer.parseInt(fields[5]);
            int totalRecovered = fields[7].isEmpty() ? 0 : Integer.parseInt(fields[7]);
            int activeCases = fields[9].isEmpty() ? 0 : Integer.parseInt(fields[9]);
            String whoRegion = fields[15];

            WorldometerDto dto = new WorldometerDto(countryName, continent, population, totalCases, totalDeaths, totalRecovered, activeCases, whoRegion);
            int hashKey = (countryName + continent + population + totalCases + totalDeaths + totalRecovered + activeCases).hashCode();
            dtoMap.put(hashKey, dto);
        }


        mapper.dtoToEntity(dtoMap);

    }
}
