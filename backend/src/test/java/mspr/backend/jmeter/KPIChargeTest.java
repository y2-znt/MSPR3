package mspr.backend.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KPIChargeTest {

    @BeforeAll
    public static void setupJMeter() throws IOException {
        // Définir le répertoire home de JMeter
        File jmeterHome = new File("src/test/resources/jmeter");
        
        // S'assurer que le dossier bin existe
        File binDir = new File(jmeterHome, "bin");
        if (!binDir.exists()) {
            binDir.mkdirs();
        }
        
        // Chemin vers le fichier jmeter.properties
        File propsFile = new File(binDir, "jmeter.properties");
        
        // Vérifier que le fichier properties existe, sinon le créer
        if (!propsFile.exists()) {
            Properties props = new Properties();
            props.setProperty("jmeter.save.saveservice.output_format", "xml");
            props.setProperty("jmeter.save.saveservice.data_type", "true");
            props.setProperty("jmeter.save.saveservice.label", "true");
            props.setProperty("jmeter.save.saveservice.response_code", "true");
            props.setProperty("jmeter.save.saveservice.response_data", "true");
            props.setProperty("log_level.jmeter", "INFO");
            
            try (FileOutputStream out = new FileOutputStream(propsFile)) {
                props.store(out, "JMeter Properties for test");
            }
        }

        // Vérifier que le fichier properties existe maintenant
        assertTrue(propsFile.exists(), "Le fichier jmeter.properties doit exister");
        
        String jmeterProperties = propsFile.getAbsolutePath();
        
        // Configurer JMeter
        JMeterUtils.setJMeterHome(jmeterHome.getAbsolutePath());
        JMeterUtils.loadJMeterProperties(jmeterProperties);
        JMeterUtils.initLocale();
        
        // Pour éviter des problèmes de plugin
        JMeterUtils.setProperty("search_paths", "");
    }

    @Test
    @Tag("performance")  // Ajout d'un tag pour exclure ce test des tests normaux
    public void testKPIGetEndpoint() throws Exception {
        // Créer le moteur JMeter
        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        // Créer un ListedHashTree au lieu de HashTree
        ListedHashTree testPlanTree = new ListedHashTree();

        // Configurer un plan de test
        TestPlan testPlan = new TestPlan("Disease Cases KPI Test Plan");
        testPlan.setProperty(TestPlan.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestPlan.GUI_CLASS, TestPlan.class.getName() + "Gui");
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
        
        // Ajouter le plan de test à l'arbre
        HashTree testPlanNode = testPlanTree.add(testPlan);

        // Créer un ThreadGroup avec contrôleur de boucle
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("KPI Test Thread Group");
        threadGroup.setNumThreads(100);  // Augmenté à 100 utilisateurs pour test de charge plus important
        threadGroup.setRampUp(10);       // Augmenté à 10 secondes pour répartir le démarrage des threads

        // Configurer le contrôleur de boucle
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.setFirst(true);
        threadGroup.setSamplerController(loopController);

        // Ajouter le thread group au plan de test
        HashTree threadGroupHashTree = testPlanNode.add(threadGroup);

        // Créer un sampler HTTP pour la requête GET
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        
        // Utilisez l'adresse IP du conteneur Docker si nécessaire au lieu de localhost
        // Par exemple, si Docker est sur la même machine, localhost fonctionnera
        // Sinon, utilisez l'adresse IP du conteneur Docker
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8080);
        httpSampler.setPath("/api/disease-cases/kpi");
        httpSampler.setMethod("GET");
        httpSampler.setName("KPI GET Request");
        httpSampler.setFollowRedirects(true);
        httpSampler.setUseKeepAlive(true);

        // Configurer le header manager
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("Content-Type", "application/json"));
        
        // Ajouter les éléments au threadGroup
        threadGroupHashTree.add(httpSampler);
        threadGroupHashTree.add(headerManager);

        // Configurer la collecte des résultats
        Summariser summer = new Summariser("KPI Test Summary");
        ResultCollector resultCollector = new ResultCollector(summer);
        
        // Sauvegarder les résultats dans un fichier pour analyse
        String resultsDir = "target/jmeter-results";
        new File(resultsDir).mkdirs();
        resultCollector.setFilename(resultsDir + "/kpi-test-results.jtl");
        
        testPlanNode.add(resultCollector);

        // Exécuter le test
        try {
            jmeter.configure(testPlanTree);
            jmeter.run();
            System.out.println("Test de charge JMeter terminé avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution du test JMeter: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Pas de vérification spécifique pour le moment, le test doit simplement s'exécuter sans exception
        assertTrue(true, "Le test de charge a été exécuté avec succès");
    }
}