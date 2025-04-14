package mspr.backend.Service.CsvImporter;

import jakarta.transaction.Transactional;
import mspr.backend.BO.Country;
import mspr.backend.BO.Region;
import mspr.backend.BO.Location;
import mspr.backend.DTO.WorldometerDto;
import mspr.backend.Mapper.WorldometerMapper;
import mspr.backend.Mapper.WorldometerMapper.CountryRegionLocation;
import mspr.backend.Repository.CountryRepository;
import mspr.backend.Repository.RegionRepository;
import mspr.backend.Repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class WorldometerService {

    public static final String FILE_NAME = "worldometer_data.csv";

    @Autowired
    private WorldometerMapper mapper;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private LocationRepository locationRepository;

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
        HashMap<Integer, WorldometerDto> dtoMap = new HashMap<>();

        // On ignore la première ligne (en-tête)
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

        // Collecte des entités uniques via le mapper
        List<Country> countries = new ArrayList<>();
        List<Region> regions = new ArrayList<>();
        List<Location> locations = new ArrayList<>();

        for (WorldometerDto dto : dtoMap.values()) {
            CountryRegionLocation triple = mapper.toEntity(dto);

            Country country = triple.getCountry();
            if (!countries.contains(country)) {
                countries.add(country);
            }
            // La région peut être nulle si le fichier n'en fournit pas
            if (triple.getRegion() != null && !regions.contains(triple.getRegion())) {
                regions.add(triple.getRegion());
            }
            if (triple.getLocation() != null && !locations.contains(triple.getLocation())) {
                locations.add(triple.getLocation());
            }
        }

        // Insertion en batch des entités récoltées
        countryRepository.saveAll(countries);
        regionRepository.saveAll(regions);
        locationRepository.saveAll(locations);

        System.out.println("Importation Worldometer terminée : " + countries.size() + " pays, " +
                regions.size() + " régions et " + locations.size() + " locations insérés.");
    }
}
