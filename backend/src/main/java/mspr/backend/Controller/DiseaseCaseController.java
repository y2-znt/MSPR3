package mspr.backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import mspr.backend.BO.DiseaseCase;
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

    @GetMapping("/{id}")
    public DiseaseCase getDiseaseCaseById(@PathVariable Integer id) {
        return diseaseCaseService.getDiseaseCaseById(id);
    }

    @GetMapping("/name/{name}")
    public DiseaseCase getDiseaseCaseByName(@PathVariable String name) {
        return diseaseCaseService.getDiseaseCaseByName(name);
    }

    @PostMapping
    public DiseaseCase createDiseaseCase(@RequestBody DiseaseCase diseaseCase) {
        return diseaseCaseService.createDiseaseCase(diseaseCase);
    }

    @PutMapping("/{id}")
    public DiseaseCase updateDiseaseCase(@PathVariable Integer id, @RequestBody DiseaseCase diseaseCase) {
        return diseaseCaseService.updateDiseaseCase(id, diseaseCase);
    }

    @DeleteMapping("/{id}")
    public void deleteDiseaseCase(@PathVariable Integer id) {
        diseaseCaseService.deleteDiseaseCase(id);
    }
}
