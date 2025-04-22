package mspr.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import mspr.backend.entity.DiseaseCase;
import mspr.backend.entity.Disease;
import mspr.backend.entity.Location;
import mspr.backend.repository.DiseaseCaseRepository;
import mspr.backend.repository.DiseaseRepository;
import mspr.backend.repository.LocationRepository;

@Service
public class DiseaseCaseService {

    @Autowired
    private DiseaseCaseRepository diseaseCaseRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private DiseaseRepository diseaseRepository;

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

    /**
     * Ajoute un nouveau cas de maladie pour un pays spécifique
     * 
     * @param countryName Nom du pays
     * @param date Date du cas
     * @param confirmedCases Nombre de cas confirmés
     * @param deaths Nombre de décès
     * @param recovered Nombre de guérisons
     * @return Le cas de maladie créé
     * @throws RuntimeException si le pays ou la maladie n'existe pas
     */
    public DiseaseCase addDiseaseCaseForCountry(
            String countryName, 
            LocalDate date, 
            Integer confirmedCases, 
            Integer deaths, 
            Integer recovered) {
        
        // Trouver la location (pays) par son nom
        List<Location> allLocations = locationRepository.findAll();
        
        // Déboguer: Afficher tous les pays pour voir comment ils sont enregistrés
        System.out.println("Recherche du pays: " + countryName);
        System.out.println("Pays disponibles dans la base de données:");
        for (Location loc : allLocations) {
            System.out.println(" - " + loc.getName());
        }
        
        // Tenter de trouver une correspondance exacte
        Location location = locationRepository.findByName(countryName);
        
        // Si aucune correspondance exacte, essayer de trouver une correspondance partielle
        if (location == null) {
            System.out.println("Aucune correspondance exacte pour: " + countryName);
            for (Location loc : allLocations) {
                if (loc.getName().toLowerCase().contains(countryName.toLowerCase()) || 
                    countryName.toLowerCase().contains(loc.getName().toLowerCase())) {
                    location = loc;
                    System.out.println("Correspondance partielle trouvée: " + loc.getName());
                    break;
                }
            }
        }
        
        // Si toujours aucune correspondance, créer le pays
        if (location == null) {
            System.out.println("Création d'un nouveau pays: " + countryName);
            location = new Location();
            location.setName(countryName);
            location = locationRepository.save(location);
        }
        
        // Obtenir la maladie (COVID-19 par défaut)
        Disease disease = diseaseRepository.findByName("COVID-19");
        if (disease == null) {
            throw new RuntimeException("Maladie 'COVID-19' non trouvée dans la base de données");
        }
        
        // Créer le nouveau cas
        DiseaseCase newCase = new DiseaseCase();
        newCase.setLocation(location);
        newCase.setDisease(disease);
        newCase.setDate(date);
        newCase.setConfirmedCases(confirmedCases);
        newCase.setDeaths(deaths);
        newCase.setRecovered(recovered);
        
        // Sauvegarder et retourner le cas
        return diseaseCaseRepository.save(newCase);
    }
}
