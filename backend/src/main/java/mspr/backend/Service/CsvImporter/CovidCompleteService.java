package mspr.backend.Service.CsvImporter;
import mspr.backend.BO.*;
import mspr.backend.DTO.CovidCompleteDto;
import mspr.backend.Helpers.*;
import mspr.backend.Mapper.CovidCompleteMapper;
import mspr.backend.Repository.*;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CovidCompleteService {

    public static final String FILE_NAME = "covid_19_clean_complete.csv";

    @Autowired
    private CovidCompleteMapper mapper;

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private DiseaseRepository diseaseRepository;


    @Autowired
    private CacheHelper cacheHelper;

    @Autowired
    private CleanerHelper cleanerHelper;

    public int importData() throws Exception {
        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        if (Files.isRegularFile(path) && Files.exists(path)) {
            System.out.println("Le fichier existe " + FILE_NAME + " et est un fichier régulier.");
        } else {
            System.out.println("ATTENTION : Le fichier " + FILE_NAME + " n'existe pas ou n'est pas un fichier régulier.");
            return 0;
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

        // Sauvegarder les entités mises en cache et remettre à jour les instances persistantes dans le cache
        Map<String, Country> countries = cacheHelper.getCountries();
        countries = countryRepository.saveAll(countries.values())
                    .stream()
                    .collect(Collectors.toMap(Country::getName, country -> country));
        cacheHelper.setCountries(countries);

        Map<String, Region> regions = cacheHelper.getRegions();
        regions = regionRepository.saveAll(regions.values())
                    .stream()
                    .collect(Collectors.toMap(r -> r.getCountry().getName() + "|" + r.getName(), region -> region));
        cacheHelper.setRegions(regions);

        Map<String, Location> locations = cacheHelper.getLocations();
        locations = locationRepository.saveAll(locations.values())
                    .stream()
                    .collect(Collectors.toMap(l -> l.getRegion().getCountry().getName() + "|" + l.getRegion().getName() + "|" + l.getName(), location -> location));
        cacheHelper.setLocations(locations);

        Map<String, Disease> diseases = cacheHelper.getDiseases();
        diseases = diseaseRepository.saveAll(diseases.values())
                        .stream()
                        .collect(Collectors.toMap(Disease::getName, disease -> disease));
        cacheHelper.setDiseases(diseases);

        for (DiseaseCase dc : diseaseCases) {
            if (dc.getDisease() != null) {
                String diseaseName = dc.getDisease().getName();
                Disease managedDisease = cacheHelper.getDiseases().get(diseaseName);
                dc.setDisease(managedDisease);
            }
        }

        // Insertion en batch de tous les DiseaseCase
        diseaseCaseRepository.saveAll(diseaseCases);
        System.out.println("Import CovidComplete terminé : " + diseaseCases.size() + " cas insérés.");
        return (lines.size()-1);
    }
}
