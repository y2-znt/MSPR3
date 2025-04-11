package mspr.backend.Runner;

import mspr.backend.BO.Disease;
import mspr.backend.Service.DiseaseService;
import mspr.backend.Service.CsvImporter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataImportRunner implements CommandLineRunner {

    @Autowired private CovidCompleteService covidCompleteService;
    @Autowired private FullGroupedService fullGroupedService;
    @Autowired private UsaCountyService usaCountyService;
    @Autowired private WorldometerService worldometerService;
    @Autowired private DiseaseService diseaseService;

    @Override
    public void run(String... args) throws Exception {
        Disease covid = diseaseService.getDiseaseByName("COVID-19");

        System.out.println("** Début de l'import des données COVID **");

        // 1. Import du snapshot Worldometer (pays, population, continent)
        System.out.println("Import des données Worldometer...");
        worldometerService.importData();
        System.out.println("Import Worldometer terminé.");

        // 2. Import des données globales détaillées par province/état
//        System.out.println("Import des données globales détaillées (covid_19_clean_complete)...");
//        covidCompleteService.importData();
//        System.out.println("Import global détaillé terminé.");

        // 3. Import des données globales agrégées par pays
//        System.out.println("Import des données globales agrégées (full_grouped)...");
//        fullGroupedService.importData();
//        System.out.println("Import global agrégé terminé.");

        // 4. Import des données par comtés US
//        System.out.println("Import des données USA par comtés...");
//        usaCountyService.importData();
//        System.out.println("Import USA par comtés terminé.");

        System.out.println("** Importation des données COVID terminée avec succès **");
    }
}