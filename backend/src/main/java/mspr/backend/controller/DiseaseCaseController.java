package mspr.backend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import mspr.backend.entity.DiseaseCase;
import mspr.backend.dto.TotalKpiDto;
import mspr.backend.service.DiseaseCaseService;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/disease-cases")
public class DiseaseCaseController {

    @Autowired
    private DiseaseCaseService diseaseCaseService;

    @GetMapping
    public Page<DiseaseCase> getAllDiseaseCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return diseaseCaseService.getAllDiseaseCases(page, size);
    }

    @GetMapping("/kpi")
    public TotalKpiDto getKpi() {
        long cases = diseaseCaseService.sumCases();
        long deaths = diseaseCaseService.sumDeaths();
        long recovered = diseaseCaseService.sumRecovered();
        return new TotalKpiDto(cases, deaths, recovered);
    }

    @GetMapping("/{id}")
    public DiseaseCase getDiseaseCaseById(@PathVariable Integer id) {
        return diseaseCaseService.getDiseaseCaseById(id).orElse(null);
    }

    @GetMapping("/name/{name}")
    public DiseaseCase getDiseaseCaseByName(@PathVariable String name) {
        return diseaseCaseService.getDiseaseCaseByName(name).orElse(null);
    }

    @GetMapping("/aggregated-by-date")
    public List<Map<String, Object>> getAggregatedCasesByDate(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) List<String> countries) {
        LocalDate startDate = (start != null) ? LocalDate.parse(start) : null;
        LocalDate endDate = (end != null) ? LocalDate.parse(end) : null;
        List<Object[]> results = diseaseCaseService.getAggregatedCasesByDateAndCountries(startDate, endDate, countries);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0]);
            map.put("country", row[1]);
            map.put("confirmedCases", row[2]);
            map.put("deaths", row[3]);
            map.put("recovered", row[4]);
            response.add(map);
        }
        return response;
    }

    @PostMapping("/aggregated-by-date")
    public Map<String, Object> addDiseaseCase(@RequestBody Map<String, Object> caseData) {
        try {
            // Extraire les données de la requête
            String dateStr = (String) caseData.get("date");
            String countryName = (String) caseData.get("country");
            Integer confirmedCases = Integer.valueOf(caseData.get("confirmedCases").toString());
            Integer deaths = Integer.valueOf(caseData.get("deaths").toString());
            Integer recovered = Integer.valueOf(caseData.get("recovered").toString());
            
            // Convertir la date
            LocalDate date = LocalDate.parse(dateStr);
            
            // Créer et enregistrer le cas
            DiseaseCase newCase = diseaseCaseService.addDiseaseCaseForCountry(
                countryName, date, confirmedCases, deaths, recovered);
            
            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", newCase.getDate());
            response.put("country", countryName);
            response.put("confirmedCases", newCase.getConfirmedCases());
            response.put("deaths", newCase.getDeaths());
            response.put("recovered", newCase.getRecovered());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @PutMapping("/aggregated-by-date/{id}")
    public Map<String, Object> updateDiseaseCase(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> caseData) {
        try {
            // Extraire les données de la requête
            String dateStr = (String) caseData.get("date");
            String countryName = (String) caseData.get("country");
            Integer confirmedCases = Integer.valueOf(caseData.get("confirmedCases").toString());
            Integer deaths = Integer.valueOf(caseData.get("deaths").toString());
            Integer recovered = Integer.valueOf(caseData.get("recovered").toString());
            
            // Convertir la date
            LocalDate date = LocalDate.parse(dateStr);
            
            // Mettre à jour et récupérer le cas
            DiseaseCase updatedCase = diseaseCaseService.updateDiseaseCaseWithDetails(
                id, countryName, date, confirmedCases, deaths, recovered);
            
            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", updatedCase.getId());
            response.put("date", updatedCase.getDate());
            response.put("country", countryName);
            response.put("confirmedCases", updatedCase.getConfirmedCases());
            response.put("deaths", updatedCase.getDeaths());
            response.put("recovered", updatedCase.getRecovered());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }
}
