package mspr.backend.Service.CsvImporter;

import mspr.backend.BO.DiseaseCase;
import mspr.backend.DTO.UsaCountyDto;
import mspr.backend.Mapper.UsaCountyMapper;
import mspr.backend.Repository.DiseaseCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class UsaCountyService {

    @Autowired
    private UsaCountyMapper mapper;
    @Autowired private DiseaseCaseRepository diseaseCaseRepository;

    public void importData() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/usa_county_wise.csv");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine(); // ignore header
            String line;
            // Formateur de date pour M/d/yy (ex: "1/22/20" -> 2020-01-22)
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("M/d/yy");
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split(",");
                // Indices d'apr. l'ordre : UID, iso2, iso3, code3, FIPS, Admin2, Province_State, Country_Region, Lat, Long_, Combined_Key, Date, Confirmed, Deaths
                String county = fields[5].trim();
                String state = fields[6].trim();
                String country = fields[7].trim(); // Devrait être "US"
                double lat = Double.parseDouble(fields[8]);
                double lon = Double.parseDouble(fields[9]);
                LocalDate date = LocalDate.parse(fields[11], dateFmt);
                int confirmed = Integer.parseInt(fields[12]);
                int deaths = Integer.parseInt(fields[13]);
                // recovered et active ne sont pas dans ce fichier:
                int recovered = 0;
                int active = 0;
                UsaCountyDto dto = new UsaCountyDto(county, state, country, lat, lon, date, confirmed, deaths, recovered, active);
                DiseaseCase diseaseCase = mapper.dtoToEntity(dto);
                diseaseCaseRepository.save(diseaseCase);
            }
        }
    }
}

