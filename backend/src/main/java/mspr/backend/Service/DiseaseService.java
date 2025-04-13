package mspr.backend.Service;

import mspr.backend.BO.Disease;
import mspr.backend.Repository.DiseaseRepository;

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

    public Disease getByNameDiseasese(String name) {
        return diseaseRepository.findByName(name);
    }

    public Disease getDiseaseByName(String name) {
        return diseaseRepository.findByName(name);
    }


}
