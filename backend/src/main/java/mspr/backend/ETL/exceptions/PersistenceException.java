package mspr.backend.ETL.exceptions;

/**
 * Exception thrown when there's an error persisting data to the database
 */
public class PersistenceException extends EtlException {
    
    public PersistenceException(String message) {
        super(message);
    }
    
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
} 