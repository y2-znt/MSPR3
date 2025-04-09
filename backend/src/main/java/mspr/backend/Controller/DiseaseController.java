package mspr.backend.Controller;

import mspr.backend.BO.Disease;
import mspr.backend.Service.DiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/diseases")  

public class DiseaseController {

    @Autowired
    private DiseaseService diseasesService;
    
    @GetMapping
    public List<Disease> getAllDiseases() {
        return diseasesService.getAllDiseases(); 
    }

    
    @GetMapping("/{id}")
    public Optional<Disease> getDiseaseById(@PathVariable Integer id) {
        return diseasesService.getDiseaseById(id); 
    }
}
