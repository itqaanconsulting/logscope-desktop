package nl.itqaanconsulting.logscope.log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogFileParserTest {

    private final LogFileParser parser = new LogFileParser();

    @Test
    void parsesStructuredLogFile(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("application.log");
        Files.writeString(file, """
                2026-06-09 10:42:18.413 ERROR [order-service] [req-91ac2] Payment failed
                2026-06-09 10:42:19.100 WARN [inventory-service] [req-91ac2] Stock is low
                2026-06-09 10:42:20.250 INFO [order-service] Order accepted
                """);

        LogAnalysis analysis = parser.parse(file);

        assertEquals(3, analysis.totalLines());
        assertEquals(1, analysis.errors());
        assertEquals(1, analysis.warnings());
        assertEquals(2, analysis.services());
        assertEquals("req-91ac2", analysis.entries().getFirst().correlationId());
        assertEquals("", analysis.entries().getLast().correlationId());
    }

    @Test
    void keepsUnrecognizedLinesVisible() {
        LogEntry entry = parser.parseLine("Caused by: connection refused");

        assertEquals("UNKNOWN", entry.level());
        assertEquals("Caused by: connection refused", entry.message());
    }
}
