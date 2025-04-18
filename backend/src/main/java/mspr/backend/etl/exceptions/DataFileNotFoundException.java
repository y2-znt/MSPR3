package mspr.backend.etl.exceptions;

/**
 * Exception thrown when a required data file is not found
 */
public class DataFileNotFoundException extends EtlException {
    
    private final String fileName;
    
    public DataFileNotFoundException(String fileName) {
        super("Required data file not found: " + fileName);
        this.fileName = fileName;
    }
    
    public DataFileNotFoundException(String fileName, Throwable cause) {
        super("Required data file not found: " + fileName, cause);
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
} 