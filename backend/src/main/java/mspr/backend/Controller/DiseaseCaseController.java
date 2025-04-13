package mspr.backend.Controller;

import java.util.Optional;

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
    public Optional<DiseaseCase> getDiseaseCaseById(@PathVariable Integer id) {
        return diseaseCaseService.getDiseaseCaseById(id);
    }
}
