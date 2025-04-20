package mspr.backend.etl.helpers.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Base abstract class for all entity caches.
 * Provides common cache management functionality.
 * 
 * @param <K> Key type for the cache
 * @param <E> Entity type stored in the cache
 */
public abstract class AbstractEntityCache<K, E> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Map<K, E> cache = new HashMap<>();
    
    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
        logger.debug("Cache cleared for {}", getClass().getSimpleName());
    }
    
    /**
     * Gets the size of the cache.
     * 
     * @return The number of entries in the cache
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Checks if the cache contains an entry for the given key.
     * 
     * @param key The key to check
     * @return true if the cache contains the key, false otherwise
     */
    public boolean contains(K key) {
        return cache.containsKey(key);
    }
    
    /**
     * Gets the entity from the cache using the key.
     * 
     * @param key The key to look up
     * @return The entity, or null if not found
     */
    public E get(K key) {
        return cache.get(key);
    }
    
    /**
     * Gets the entire cache map.
     * 
     * @return The map containing all cached entities
     */
    public Map<K, E> getAll() {
        return new HashMap<>(cache);
    }
    
    /**
     * Adds an entity to the cache.
     * 
     * @param key The key for the entity
     * @param entity The entity to cache
     */
    public void put(K key, E entity) {
        if (key != null && entity != null) {
            cache.put(key, entity);
        }
    }
} 