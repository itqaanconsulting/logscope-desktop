package nl.itqaanconsulting.logscope.log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogCsvExporterTest {

    private final LogCsvExporter exporter = new LogCsvExporter();

    @Test
    void escapesCsvSpecialCharacters() {
        assertEquals("plain", exporter.escape("plain"));
        assertEquals("\"contains,comma\"", exporter.escape("contains,comma"));
        assertEquals("\"contains \"\"quotes\"\"\"", exporter.escape("contains \"quotes\""));
        assertEquals("\"first\nsecond\"", exporter.escape("first\nsecond"));
    }

    @Test
    void writesHeaderAndEntriesWithUtf8Bom(@TempDir Path directory) throws IOException {
        Path target = directory.resolve("filtered.csv");
        LogEntry entry = new LogEntry(
                "2026-06-10T10:30:00+02:00",
                "ERROR",
                "order-service",
                "worker-1",
                "OrderService",
                "Failed, retrying",
                "trace-42",
                "java.lang.IllegalStateException: \"invalid\""
        );

        exporter.export(target, List.of(entry));

        String csv = Files.readString(target, StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFFtimestamp,level,service"));
        assertTrue(csv.contains("\"Failed, retrying\""));
        assertTrue(csv.contains("\"java.lang.IllegalStateException: \"\"invalid\"\"\""));
    }
}
