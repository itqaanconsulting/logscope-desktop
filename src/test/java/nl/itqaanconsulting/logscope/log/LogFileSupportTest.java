package nl.itqaanconsulting.logscope.log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogFileSupportTest {

    @Test
    void recognizesSupportedExtensionsCaseInsensitively(@TempDir Path directory) throws IOException {
        Path logFile = Files.createFile(directory.resolve("application.LOG"));
        Path jsonFile = Files.createFile(directory.resolve("events.jsonl"));

        assertTrue(LogFileSupport.isSupported(logFile.toFile()));
        assertTrue(LogFileSupport.isSupported(jsonFile.toFile()));
    }

    @Test
    void rejectsDirectoriesAndUnsupportedFiles(@TempDir Path directory) throws IOException {
        Path image = Files.createFile(directory.resolve("screenshot.png"));

        assertFalse(LogFileSupport.isSupported(directory.toFile()));
        assertFalse(LogFileSupport.isSupported(image.toFile()));
    }

    @Test
    void selectsFirstSupportedFile(@TempDir Path directory) throws IOException {
        Path image = Files.createFile(directory.resolve("screenshot.png"));
        Path log = Files.createFile(directory.resolve("service.log"));
        Path json = Files.createFile(directory.resolve("events.json"));

        assertEquals(
                log.toFile(),
                LogFileSupport.firstSupported(List.of(image.toFile(), log.toFile(), json.toFile())).orElseThrow()
        );
    }
}
