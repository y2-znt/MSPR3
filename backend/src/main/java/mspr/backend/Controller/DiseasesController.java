package mspr.backend.Controller;

import mspr.backend.BO.Diseases;
import mspr.backend.Service.DiseasesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/diseases")  

public class DiseasesController {

    @Autowired
    private DiseasesService diseasesService;
    
    @GetMapping
    public List<Diseases> getAllDiseases() {
        return diseasesService.getAllDiseases(); 
    }

    
    @GetMapping("/{id}")
    public Optional<Diseases> getDiseaseById(@PathVariable Integer id) {
        return diseasesService.getDiseaseById(id); 
    }
}
