package mspr.backend.etl.helpers.cache;

import mspr.backend.entity.Disease;
import org.springframework.stereotype.Component;

/**
 * Cache for Disease entities.
 * Uses disease name as the key.
 */
@Component
public class DiseaseCache extends AbstractEntityCache<String, Disease> {

    /**
     * Gets or creates a Disease entity with the given name.
     *
     * @param diseaseName The name of the disease
     * @return The Disease entity, or null if diseaseName is null/empty
     */
    public Disease getOrCreate(String diseaseName) {
        if (diseaseName == null || diseaseName.isEmpty()) {
            return null;
        }
        
        Disease disease = get(diseaseName);
        if (disease == null) {
            disease = new Disease();
            disease.setName(diseaseName);
            put(diseaseName, disease);
            logger.debug("Created new disease: {}", diseaseName);
        }
        
        return disease;
    }

    /**
     * Updates the disease cache from an iterable of saved/managed Disease entities.
     *
     * @param savedDiseases An iterable of Disease entities, typically from saveAll
     */
    public void updateCache(Iterable<Disease> savedDiseases) {
        clear();
        if (savedDiseases != null) {
            for (Disease disease : savedDiseases) {
                if (disease != null && disease.getName() != null) {
                    put(disease.getName(), disease);
                }
            }
            logger.debug("Updated disease cache with {} entities", size());
        }
    }
} 