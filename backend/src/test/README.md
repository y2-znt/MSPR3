# Configuration des Tests d'Intégration Sans ETL

Ce projet est configuré pour exécuter des tests d'intégration avec une base de données H2 en mémoire, sans exécuter le processus ETL qui charge les données COVID-19. Cette approche accélère considérablement l'exécution des tests.

## Configuration

### Profil de Test

Les tests d'intégration utilisent le profil Spring `test`, qui est activé via l'annotation `@ActiveProfiles("test")`. Ce profil :

1. Utilise une base de données H2 en mémoire au lieu de PostgreSQL
2. Désactive automatiquement l'ETL grâce à la condition `@Profile("!test")` sur le `DataImportCovid19Runner`

### Fichiers de Configuration

- `application-test.properties` : Configuration spécifique pour les tests H2
- `bootstrap.properties` : Variables d'environnement pour les tests

### Adaptation des Tests aux Comportements de l'API

Les tests d'intégration ont été adaptés pour refléter le comportement réel de l'API :

1. **Pagination** : Utilisation de `ParameterizedTypeReference<Map<String, Object>>` pour récupérer les données paginées
2. **Gestion des Null** : Vérification que les méthodes retournent `null` (et non 404) pour les ressources non trouvées
3. **Endpoints** : Tests de disponibilité des endpoints racine (`/`) pour vérifier que le serveur est opérationnel

## Exécution des Tests

Pour exécuter l'ensemble des tests d'intégration :

```bash
mvn test
```

Pour un test spécifique :

```bash
mvn test -Dtest=CountryControllerIntegrationTest
```

Pour exécuter une méthode de test spécifique :

```bash
mvn test -Dtest=CountryControllerIntegrationTest#testCreateCountry
```

## Création de Données de Test

Au lieu de dépendre de l'ETL pour charger les données, vous devez créer vos propres données de test dans chaque test. Vous pouvez le faire de plusieurs façons :

1. **En ligne dans chaque test** (comme dans `CountryControllerIntegrationTest`)
2. **Utiliser des fixtures** : créer des méthodes utilitaires qui génèrent des jeux de données de test
3. **Scripts SQL** : utiliser des scripts SQL pour initialiser la base de données

### Exemple Avec des Fixtures

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestWithFixtures {

    @Autowired
    private CountryRepository countryRepository;

    @BeforeAll
    public void setupTestData() {
        // Créer vos données de test ici
        createTestCountries();
    }

    private void createTestCountries() {
        Country france = new Country();
        france.setName("France");
        france.setContinent(Country.ContinentEnum.EUROPE);
        france.setWhoRegion(Country.WHORegionEnum.Europe);
        countryRepository.save(france);

        // Autres pays...
    }
}
```

## Configuration interne de CommandLineRunner

Les tests utilisent une configuration interne pour désactiver tous les CommandLineRunner :

```java
@TestConfiguration
static class TestConfig {
    @Bean
    @Primary
    public CommandLineRunner noOpCommandLineRunner() {
        return args -> {
            // Message indiquant que l'ETL est désactivé
        };
    }
}
```

## Avantages

- **Performances** : Tests plus rapides (pas de chargement ETL)
- **Isolation** : Environnement de test isolé et contrôlé
- **Fiabilité** : Contrôle précis des données de test
- **Indépendance** : Tests des fonctionnalités sans dépendre de la qualité/disponibilité des données externes
- **Reproductibilité** : Les tests peuvent être exécutés sans configuration externe
