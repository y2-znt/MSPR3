package mspr.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mspr.backend.BO.DiseaseCase;
import mspr.backend.Repository.DiseaseCaseRepository;

@Service
public class DiseaseCaseService {

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;

    public List<DiseaseCase> getAllDiseaseCases() {
        return diseaseCaseRepository.findAll();
    }

    public Optional<DiseaseCase> getDiseaseCaseById(Integer id) {
        return diseaseCaseRepository.findById(id);
    }

}
