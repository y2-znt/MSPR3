package mspr.backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import mspr.backend.BO.DiseaseCase;
import mspr.backend.DTO.TotalKpiDto;
import mspr.backend.Service.DiseaseCaseService;

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
}
