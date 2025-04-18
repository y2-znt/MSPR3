package mspr.backend.Service.CsvImporter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mspr.backend.BO.*;
import mspr.backend.DTO.FullGroupedDto;
import mspr.backend.DTO.UsaCountyDto;
import mspr.backend.Helpers.*;
import mspr.backend.Mapper.UsaCountyMapper;
import mspr.backend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.transaction.Transactional;

@Service
public class UsaCountyService {

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private LocationRepository locationRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UsaCountyMapper usaCountyMapper;


    public static final String FILE_NAME = "usa_county_wise.csv";

    @Transactional
    public int importData() {
        // Initialisation du cache en mémoire pour les entités de référence
        CacheHelper cache = new CacheHelper();

        // Lecture du fichier CSV et conversion en DTO (déjà en place)
        List<UsaCountyDto> dtos = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        if (Files.isRegularFile(path) && Files.exists(path)) {
            System.out.println("Le fichier " + FILE_NAME + " existe et est un fichier régulier.");
        } else {
            System.out.println("ATTENTION : Le fichier " + FILE_NAME + " n'existe pas ou n'est pas un fichier régulier.");
            return 0;
        }

        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            // Définition du format de date tel que "M/d/yy" (par ex. "1/22/20")
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("M/d/yy");

            // On commence à l'index 1 pour ignorer l'en-tête
            for (int l = 1; l < lines.size(); l++) {
                String line = lines.get(l);
                // Expression régulière pour traiter correctement les virgules dans des champs entre guillemets
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Indices attendus (selon l'ordre du fichier usa_county_wise.csv) :
                // 5: Admin2 (nom du comté), 6: Province_State, 7: Country_Region,
                // 8: Lat, 9: Long_, 11: Date, 12: Confirmed, 13: Deaths
                String county = fields[5].trim();
                String provinceState = fields[6].trim();
                String countryRegion = fields[7].trim();
                double lat = Double.parseDouble(fields[8].trim());
                double lon = Double.parseDouble(fields[9].trim());
                LocalDate date = LocalDate.parse(fields[11].trim(), dateFmt);
                int confirmed = fields[12].isEmpty() ? 0 : Integer.parseInt(fields[12].trim());
                int deaths = fields[13].isEmpty() ? 0 : Integer.parseInt(fields[13].trim());
                // Les données "recovered" et "active" ne sont pas présentes dans ce fichier
                int recovered = 0;
                int active = 0;

                UsaCountyDto dto = new UsaCountyDto(county, provinceState, countryRegion, lat, lon, date, confirmed, deaths, recovered, active);
                dtos.add(dto);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<DiseaseCase> batchList = new ArrayList<>();
        int batchSize = 1000;
        for (UsaCountyDto dto : dtos) {
            // Utilisation du mapper pour convertir le DTO en entité DiseaseCase avec références du cache
            DiseaseCase diseaseCase = usaCountyMapper.fromDto(dto, cache);

            // Si une nouvelle entité de référence a été créée, la sauvegarder immédiatement (pour obtenir un ID)
            Country country = diseaseCase.getLocation().getRegion().getCountry();
            if (country.getId() == null) {
                countryRepository.save(country);
            }
            Region region = diseaseCase.getLocation().getRegion();
            if (region.getId() == null) {
                regionRepository.save(region);
            }
            Location location = diseaseCase.getLocation();
            if (location.getId() == null) {
                locationRepository.save(location);
            }

            batchList.add(diseaseCase);
            if (batchList.size() >= batchSize) {
                // Sauvegarde en lot des DiseaseCase
                diseaseCaseRepository.saveAll(batchList);
                entityManager.flush();
                entityManager.clear();
                batchList.clear();
            }
        }
        // Insérer les éventuels derniers enregistrements si la liste n’est pas vide
        if (!batchList.isEmpty()) {
            diseaseCaseRepository.saveAll(batchList);
            entityManager.flush();
            entityManager.clear();
            batchList.clear();
        }
        return (lines.size()-1);
    }

}


