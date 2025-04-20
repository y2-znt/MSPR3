package mspr.backend.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public long sumCases() {
        return diseaseCaseRepository.sumCases();
    }

    public long sumDeaths() {
        return diseaseCaseRepository.sumDeaths();
    }

    public long sumRecovered() {
        return diseaseCaseRepository.sumRecovered();
    }

    // public Optional<DiseaseCase> getDiseaseCaseByName(String name) {
    // return diseaseCaseRepository.findAll().stream()
    // .filter(diseaseCase -> diseaseCase.getName().equalsIgnoreCase(name))
    // .findFirst();
    // }

    public Optional<DiseaseCase> getDiseaseCaseByName(String name) {
        return Optional.ofNullable(diseaseCaseRepository.findByName(name));
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

    public List<Object[]> getAggregatedCasesByDateBetween(LocalDate start, LocalDate end) {
        List<Object[]> all = diseaseCaseRepository.getAggregatedCasesByDate();
        if (start == null && end == null)
            return all;
        List<Object[]> filtered = new ArrayList<>();
        for (Object[] row : all) {
            LocalDate date = (LocalDate) row[0];
            boolean afterStart = (start == null) || !date.isBefore(start);
            boolean beforeEnd = (end == null) || !date.isAfter(end);
            if (afterStart && beforeEnd)
                filtered.add(row);
        }
        return filtered;
    }

    public List<Object[]> getAggregatedCasesByDateAndCountries(LocalDate start, LocalDate end, List<String> countries) {
        List<Object[]> all;
        if (countries == null || countries.isEmpty()) {
            all = diseaseCaseRepository.getAggregatedCasesByDateAllCountries();
        } else {
            all = diseaseCaseRepository.getAggregatedCasesByDateAndCountries(countries);
        }
        if (start == null && end == null) return all;
        List<Object[]> filtered = new ArrayList<>();
        for (Object[] row : all) {
            LocalDate date = (LocalDate) row[0];
            boolean afterStart = (start == null) || !date.isBefore(start);
            boolean beforeEnd = (end == null) || !date.isAfter(end);
            if (afterStart && beforeEnd) filtered.add(row);
        }
        return filtered;
    }
}
