package mspr.backend.Service.CsvImporter;

import jakarta.transaction.Transactional;
import mspr.backend.BO.*;
import mspr.backend.DTO.FullGroupedDto;
import mspr.backend.Helpers.*;
import mspr.backend.Mapper.FullGroupedMapper;
import mspr.backend.Repository.*;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FullGroupedService {

    public static final String FILE_NAME = "full_grouped.csv";

    @Autowired
    private FullGroupedMapper mapper;

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
    private CleanerHelper cleanerHelper;
    @Autowired
    private CacheHelper cacheHelper;

    public int importData() throws Exception {
        String pathFile = "src/main/resources/data/" + FILE_NAME;
        Path path = Paths.get(pathFile);

        if (Files.isRegularFile(path) && Files.exists(path)) {
            System.out.println("Le fichier " + FILE_NAME + " existe et est un fichier régulier.");
        } else {
            System.out.println("ATTENTION : Le fichier " + FILE_NAME + " n'existe pas ou n'est pas un fichier régulier.");
            return 0;
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



        // Sauvegarde en batch des entités mis en cache pour éviter les erreurs de déconnexion (detached entity)
        // 1. Countries
        Map<String, Country> countries = cacheHelper.getCountries();
        countries = countryRepository.saveAll(countries.values())
                    .stream()
                    .collect(Collectors.toMap(Country::getName, country -> country));
        cacheHelper.setCountries(countries);

        // 2. Regions
        Map<String, Region> regions = cacheHelper.getRegions();
        regions = regionRepository.saveAll(regions.values())
                    .stream()
                    .collect(Collectors.toMap(r -> r.getCountry().getName() + "|" + r.getName(), region -> region));
        cacheHelper.setRegions(regions);

        // 3. Locations
        Map<String, Location> locations = cacheHelper.getLocations();
        locations = locationRepository.saveAll(locations.values())
                    .stream()
                    .collect(Collectors.toMap(l -> l.getRegion().getCountry().getName() + "|" + l.getRegion().getName() + "|" + l.getName(), location -> location));
        cacheHelper.setLocations(locations);


        // 4. Diseases
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
        System.out.println("Import Full Grouped terminé : " + diseaseCases.size() + " cas insérés.");
        return (lines.size()-1);
    }
}
