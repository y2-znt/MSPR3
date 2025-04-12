package mspr.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    public Page<DiseaseCase> getAllDiseaseCases(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return diseaseCaseRepository.findAll(pageable);
    }

    public Optional<DiseaseCase> getDiseaseCaseById(Integer id) {
        return diseaseCaseRepository.findById(id);
    }

    public Optional<DiseaseCase> getDiseaseCaseByName(String name) {
        return diseaseCaseRepository.findAll().stream()
                .filter(diseaseCase -> diseaseCase.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public DiseaseCase createDiseaseCase(DiseaseCase diseaseCase) {
        return diseaseCaseRepository.save(diseaseCase);
    }

    public DiseaseCase updateDiseaseCase(Integer id, DiseaseCase diseaseCase) {
        if (diseaseCaseRepository.existsById(id)) {
            diseaseCase.setId(id);
            return diseaseCaseRepository.save(diseaseCase);
        } else {
            return null;
        }
    }

    public void deleteDiseaseCase(Integer id) {
        diseaseCaseRepository.deleteById(id);
    }



}
