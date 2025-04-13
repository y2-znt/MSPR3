package mspr.backend.Controller;

import mspr.backend.BO.Disease;
import mspr.backend.Service.DiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diseases")  

public class DiseaseController {

    @Autowired
    private DiseaseService diseaseService;
    
    @GetMapping
    public List<Disease> getAllDiseases() {
        return diseaseService.getAllDiseases(); 
    }

    @GetMapping("/{id}")
    public Disease getDiseaseById(@PathVariable Integer id) {
        return diseaseService.getDiseaseById(id).orElse(null); 
    }

    @GetMapping("/name/{name}")
    public Disease getDiseaseByName(@PathVariable String name) {
        return diseaseService.getDiseaseByName(name); 
    }

    @PostMapping
    public Disease createDisease(@RequestBody Disease disease) {
        return diseaseService.createDisease(disease); 
    }   

    @PutMapping("/{id}")
    public Disease updateDisease(@PathVariable Integer id, @RequestBody Disease disease) {
        return diseaseService.updateDisease(id, disease); 
    }

    @DeleteMapping("/{id}")
    public void deleteDisease(@PathVariable Integer id) {
        diseaseService.deleteDisease(id);
    }
}
