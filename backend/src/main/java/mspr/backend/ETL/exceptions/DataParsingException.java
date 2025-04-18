package mspr.backend.ETL.exceptions;

/**
 * Exception thrown when there's an error parsing data from a file
 */
public class DataParsingException extends EtlException {
    
    private final String fileName;
    private final int lineNumber;
    
    public DataParsingException(String fileName, int lineNumber, String message) {
        super(String.format("Error parsing data at line %d in file %s: %s", lineNumber, fileName, message));
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
    
    public DataParsingException(String fileName, int lineNumber, String message, Throwable cause) {
        super(String.format("Error parsing data at line %d in file %s: %s", lineNumber, fileName, message), cause);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
} 