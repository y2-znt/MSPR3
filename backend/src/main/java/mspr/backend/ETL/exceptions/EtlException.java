package mspr.backend.ETL.exceptions;

/**
 * Base exception for all ETL-related errors
 */
public class EtlException extends Exception {
    
    public EtlException(String message) {
        super(message);
    }
    
    public EtlException(String message, Throwable cause) {
        super(message, cause);
    }
} 