# Configuration des Tests d'Intégration Sans ETL

Ce projet est configuré pour exécuter des tests d'intégration avec une base de données H2 en mémoire, sans exécuter le processus ETL qui charge les données COVID-19. Cette approche accélère considérablement l'exécution des tests.

## Configuration

### Profil de Test

Les tests d'intégration utilisent le profil Spring `test`, qui est activé via l'annotation `@ActiveProfiles("test")`. Ce profil :

1. Utilise une base de données H2 en mémoire au lieu de PostgreSQL
2. Désactive automatiquement l'ETL grâce à la condition `@Profile("!test")` sur le `DataImportCovid19Runner`

### Fichiers de Configuration

- `application-test.properties` : Configuration spécifique pour les tests H2

### Adaptation des Tests aux Comportements de l'API

Les tests d'intégration ont été adaptés pour refléter le comportement réel de l'API :

1. **Pagination** : Utilisation de `ParameterizedTypeReference<Map<String, Object>>` pour récupérer les données paginées
2. **Gestion des ressources non trouvées** : Vérification que les endpoints API retournent un code HTTP 404 (NOT_FOUND) pour les ressources qui n'existent pas
3. **Endpoints** : Tests de disponibilité des endpoints racine (`/`) pour vérifier que le serveur est opérationnel

### Bonnes Pratiques REST Implémentées

Le contrôleur API suit les bonnes pratiques REST :

1. **Codes de statut appropriés** :
   - 200 OK pour les requêtes réussies
   - 404 NOT_FOUND pour les ressources inexistantes
2. **Format de réponse cohérent** : Utilisation de `ResponseEntity<T>` pour contrôler précisément les codes de statut et les corps de réponse
3. **Pagination** : Implémentation de la pagination pour les listes de ressources

## Exécution des Tests

Pour exécuter l'ensemble des tests d'intégration :

```bash
mvn test
```

Pour un test spécifique :

```bash
mvn test -Dtest=CountryControllerTest
```

Pour exécuter une méthode de test spécifique :

```bash
mvn test -Dtest=CountryControllerTest#testCreateCountry
```

## Création de Données de Test

Au lieu de dépendre de l'ETL pour charger les données, vous devez créer vos propres données de test dans chaque test. Vous pouvez le faire de plusieurs façons :

1. **En ligne dans chaque test** (comme dans `CountryControllerTest`)
2. **Utiliser des fixtures** : créer des méthodes utilitaires qui génèrent des jeux de données de test
3. **Scripts SQL** : utiliser des scripts SQL pour initialiser la base de données

## Structure des Tests

Les tests suivent le pattern AAA (Arrange-Act-Assert) :

```java
@Test
@DisplayName("should update a country when called PUT /api/countries/{id}")
public void testUpdateCountry() {
    // Arrange
    Country country = createTestCountry("France");
    country.setName("France Updated");

    // Action
    HttpEntity<Country> requestEntity = new HttpEntity<>(country);
    ResponseEntity<Country> response = restTemplate.exchange(
            baseUrl + "/" + country.getId(),
            HttpMethod.PUT,
            requestEntity,
            Country.class);

    // Assertion
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Country updatedCountry = response.getBody();
    assertNotNull(updatedCountry);
    assertEquals("France Updated", updatedCountry.getName());
}
```

## Avantages

- **Performances** : Tests plus rapides (pas de chargement ETL)
- **Isolation** : Environnement de test isolé et contrôlé
- **Fiabilité** : Contrôle précis des données de test
- **Indépendance** : Tests des fonctionnalités sans dépendre de la qualité/disponibilité des données externes
- **Reproductibilité** : Les tests peuvent être exécutés sans configuration externe
- **Conformité** : Respect des bonnes pratiques REST
