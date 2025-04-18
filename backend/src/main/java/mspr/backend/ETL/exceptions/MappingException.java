package mspr.backend.ETL.exceptions;

/**
 * Exception thrown when there's an error mapping data between DTO and entity objects
 */
public class MappingException extends EtlException {
    
    public MappingException(String message) {
        super(message);
    }
    
    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }
} 