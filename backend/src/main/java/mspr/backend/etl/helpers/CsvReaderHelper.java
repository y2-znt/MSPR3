package mspr.backend.etl.helpers;

import mspr.backend.etl.exceptions.DataFileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

@Component
public class CsvReaderHelper {

    private static final Logger logger = LoggerFactory.getLogger(CsvReaderHelper.class);

    /**
     * Reads a CSV file and returns its lines.
     *
     * @param filePath Path to the CSV file.
     * @param fileName The simple name of the file (for logging and exceptions).
     * @return List of lines from the CSV file.
     * @throws IOException If there is an error reading the file.
     * @throws DataFileNotFoundException If the file is not found at the specified path.
     */
    public List<String> readCsvFile(Path filePath, String fileName) throws IOException, DataFileNotFoundException {
        // Check if file exists first
        if (!Files.isRegularFile(filePath) || !Files.exists(filePath)) {
            logger.error("File {} does not exist or is not a regular file at path {}. Import aborted.", fileName, filePath);
            throw new DataFileNotFoundException(fileName);
        }

        logger.info("Reading file: {}", fileName);
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            logger.info("Read {} lines from {}", lines.size(), fileName);
            return lines;
        } catch (NoSuchFileException e) {
            // This might be redundant due to the initial check, but good practice
            logger.error("File not found during read operation for {}: {}", fileName, e.getMessage());
            throw new DataFileNotFoundException(fileName, e);
        } catch (IOException e) {
            logger.error("IO error reading file {}: {}", fileName, e.getMessage());
            throw e; // rethrow as it's already a specific exception
        }
    }
} 