package mspr.backend.Service;

import mspr.backend.BO.Diseases;
import mspr.backend.Repository.DiseasesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiseasesService {

    @Autowired
    private DiseasesRepository diseasesRepository;

    public List<Diseases> getAllDiseases() {
        return diseasesRepository.findAll();
    }

    public Optional<Diseases> getDiseaseById(Integer id) {
        return diseasesRepository.findById(id);
    }

    public Diseases getByNameDiseasese(String name) {
        return diseasesRepository.findByName(name);
    }
}
