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
    private DiseaseRepository diseasesRepository;

    public List<Disease> getAllDiseases() {
        return diseasesRepository.findAll();
    }

    public Optional<Disease> getDiseaseById(Integer id) {
        return diseasesRepository.findById(id);
    }

    public Disease getByNameDiseasese(String name) {
        return diseasesRepository.findByName(name);
    }
}
