package mspr.backend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mspr.backend.dto.TotalKpiDto;
import mspr.backend.entity.DiseaseCase;
import mspr.backend.service.DiseaseCaseService;

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
            map.put("id", row[5]);
            response.add(map);
        }
        return response;
    }

    @PostMapping("/aggregated-by-date")
    public Map<String, Object> addDiseaseCase(@RequestBody Map<String, Object> caseData) {
        try {
            String dateStr = (String) caseData.get("date");
            String countryName = (String) caseData.get("country");
            Integer confirmedCases = Integer.valueOf(caseData.get("confirmedCases").toString());
            Integer deaths = Integer.valueOf(caseData.get("deaths").toString());
            Integer recovered = Integer.valueOf(caseData.get("recovered").toString());
            
            LocalDate date = LocalDate.parse(dateStr);
            
            DiseaseCase newCase = diseaseCaseService.addDiseaseCaseForCountry(
                countryName, date, confirmedCases, deaths, recovered);
            
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
            String dateStr = (String) caseData.get("date");
            String countryName = (String) caseData.get("country");
            Integer confirmedCases = Integer.valueOf(caseData.get("confirmedCases").toString());
            Integer deaths = Integer.valueOf(caseData.get("deaths").toString());
            Integer recovered = Integer.valueOf(caseData.get("recovered").toString());
            
            LocalDate date = LocalDate.parse(dateStr);
            
            DiseaseCase updatedCase = diseaseCaseService.updateDiseaseCaseWithDetails(
                id, countryName, date, confirmedCases, deaths, recovered);
            
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

    @DeleteMapping("/aggregated-by-date/latest/{country}")
    public Map<String, Object> deleteLatestDiseaseCase(@PathVariable String country) {
        try {
            boolean deleted = diseaseCaseService.deleteLatestCaseForCountry(country);
            
            Map<String, Object> response = new HashMap<>();
            if (deleted) {
                response.put("success", true);
                response.put("message", "La dernière entrée pour " + country + " a été supprimée avec succès");
            } else {
                response.put("success", false);
                response.put("error", "Aucune entrée trouvée pour " + country);
            }
            
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @DeleteMapping("/aggregated-by-date/{id}")
    public Map<String, Object> deleteDiseaseCase(@PathVariable Integer id) {
        try {
            System.out.println("Backend - Requête de suppression reçue pour l'ID: " + id);
            
            Optional<DiseaseCase> existingCase = diseaseCaseService.getDiseaseCaseById(id);
            boolean exists = existingCase.isPresent();
            System.out.println("Backend - Le cas existe-t-il? " + exists);
            
            if (!exists) {
                System.out.println("Backend - Cas non trouvé, erreur");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Cas non trouvé avec l'ID: " + id);
                return errorResponse;
            }
            
            System.out.println("Backend - Suppression du cas avec l'ID: " + id);
            diseaseCaseService.deleteDiseaseCase(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Le cas a été supprimé avec succès");
            System.out.println("Backend - Suppression réussie, réponse: " + response);
            
            return response;
        } catch (Exception e) {
            System.err.println("Backend - Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }
}
