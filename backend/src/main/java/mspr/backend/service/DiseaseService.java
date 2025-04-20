package mspr.backend.service;

import mspr.backend.entity.Disease;
import mspr.backend.repository.DiseaseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiseaseService {

    @Autowired
    private DiseaseRepository diseaseRepository;

    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAll();
    }

    public Optional<Disease> getDiseaseById(Integer id) {
        return diseaseRepository.findById(id);
    }

    public Disease getDiseaseByName(String name) {
        return diseaseRepository.findByName(name);
    }


    public Disease createDisease(Disease disease) {
        return diseaseRepository.save(disease);
    }

    public Disease updateDisease(Integer id, Disease disease) {
        if (diseaseRepository.existsById(id)) {
            disease.setId(id);
            return diseaseRepository.save(disease);
        } else {
            return null;
        }
    }

    public void deleteDisease(Integer id) {
        diseaseRepository.deleteById(id);
    }
    
}
