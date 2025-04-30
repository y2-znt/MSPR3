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

    public DiseaseCase addDiseaseCaseForCountry(
            String countryName, 
            LocalDate date, 
            Integer confirmedCases, 
            Integer deaths, 
            Integer recovered) {
        
        // Find location by name
        List<Location> allLocations = locationRepository.findAll();
        
        // Find exact match first
        Location location = locationRepository.findByName(countryName);
        
        // if not found, try to find a partial match
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
        
        // create
        if (location == null) {
            System.out.println("Création d'un nouveau pays: " + countryName);
            location = new Location();
            location.setName(countryName);
            location = locationRepository.save(location);
        }
        
        // get disease by name
        Disease disease = diseaseRepository.findByName("COVID-19");
        if (disease == null) {
            throw new RuntimeException("Maladie 'COVID-19' non trouvée dans la base de données");
        }
        
        // create new case
        DiseaseCase newCase = new DiseaseCase();
        newCase.setLocation(location);
        newCase.setDisease(disease);
        newCase.setDate(date);
        newCase.setConfirmedCases(confirmedCases);
        newCase.setDeaths(deaths);
        newCase.setRecovered(recovered);
        
        return diseaseCaseRepository.save(newCase);
    }

    /* update  */
    
    public DiseaseCase updateDiseaseCaseWithDetails(
            Integer id,
            String countryName,
            LocalDate date,
            Integer confirmedCases,
            Integer deaths,
            Integer recovered) {
        
        // check if case exists
        Optional<DiseaseCase> existingCaseOpt = diseaseCaseRepository.findById(id);
        if (existingCaseOpt.isEmpty()) {
            throw new RuntimeException("Cas non trouvé avec l'ID: " + id);
        }
        
        DiseaseCase existingCase = existingCaseOpt.get();
        
        // Si le pays a changé, trouver le nouveau pays
        if (!existingCase.getLocation().getName().equals(countryName)) {
            // Utiliser la même logique que pour l'ajout pour trouver/créer le pays
            List<Location> allLocations = locationRepository.findAll();
            
            // Debug log
            System.out.println("Mise à jour - Recherche du pays: " + countryName);
            
            // Tenter de trouver une correspondance exacte
            Location location = locationRepository.findByName(countryName);
            
            // Si aucune correspondance exacte, essayer de trouver une correspondance partielle
            if (location == null) {
                System.out.println("Mise à jour - Aucune correspondance exacte pour: " + countryName);
                for (Location loc : allLocations) {
                    if (loc.getName().toLowerCase().contains(countryName.toLowerCase()) || 
                        countryName.toLowerCase().contains(loc.getName().toLowerCase())) {
                        location = loc;
                        System.out.println("Mise à jour - Correspondance partielle trouvée: " + loc.getName());
                        break;
                    }
                }
            }
            
            // Si toujours aucune correspondance, créer le pays
            if (location == null) {
                System.out.println("Mise à jour - Création d'un nouveau pays: " + countryName);
                location = new Location();
                location.setName(countryName);
                location = locationRepository.save(location);
            }
            
            existingCase.setLocation(location);
        }
        
        // Mettre à jour les autres champs
        existingCase.setDate(date);
        existingCase.setConfirmedCases(confirmedCases);
        existingCase.setDeaths(deaths);
        existingCase.setRecovered(recovered);
        
        // Sauvegarder et retourner le cas mis à jour
        return diseaseCaseRepository.save(existingCase);
    }

    /* delete last */
    public boolean deleteLatestCaseForCountry(String countryName) {
        // find the latest case for the country
        List<DiseaseCase> cases = diseaseCaseRepository.findByLocationNameOrderByDateDesc(countryName);
        
        // if no cases found, return false
        if (cases == null || cases.isEmpty()) {
            return false;
        }
        
        // delete the latest case
        diseaseCaseRepository.delete(cases.get(0));
        return true;
    }
}
