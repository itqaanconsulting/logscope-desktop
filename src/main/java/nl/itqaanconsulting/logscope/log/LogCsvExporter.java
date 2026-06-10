package nl.itqaanconsulting.logscope.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LogCsvExporter {

    private static final List<String> HEADER = List.of(
            "timestamp",
            "level",
            "service",
            "thread",
            "logger",
            "message",
            "correlationId",
            "details"
    );

    public void export(Path target, List<LogEntry> entries) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writeRow(writer, HEADER);

            for (LogEntry entry : entries) {
                writeRow(writer, List.of(
                        entry.timestamp(),
                        entry.level(),
                        entry.service(),
                        entry.thread(),
                        entry.logger(),
                        entry.message(),
                        entry.correlationId(),
                        entry.details()
                ));
            }
        }
    }

    private void writeRow(BufferedWriter writer, List<String> values) throws IOException {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                writer.write(',');
            }
            writer.write(escape(values.get(index)));
        }
        writer.newLine();
    }

    String escape(String value) {
        String safeValue = value == null ? "" : value;
        boolean requiresQuotes = safeValue.indexOf(',') >= 0
                || safeValue.indexOf('"') >= 0
                || safeValue.indexOf('\n') >= 0
                || safeValue.indexOf('\r') >= 0;

        if (!requiresQuotes) {
            return safeValue;
        }
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
