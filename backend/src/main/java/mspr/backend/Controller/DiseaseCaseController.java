package mspr.backend.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mspr.backend.BO.DiseaseCase;
import mspr.backend.Service.DiseaseCaseService;


@RestController
@RequestMapping("/api/disease-cases")
public class DiseaseCaseController {

    @Autowired
    private DiseaseCaseService diseaseCaseService;

    @GetMapping
    public List<DiseaseCase> getAllDiseaseCases() {
        return diseaseCaseService.getAllDiseaseCases();
    }


    @GetMapping("/{id}")
    public Optional<DiseaseCase> getDiseaseCaseById(@PathVariable Integer id) {
        return diseaseCaseService.getDiseaseCaseById(id);
    }
}
