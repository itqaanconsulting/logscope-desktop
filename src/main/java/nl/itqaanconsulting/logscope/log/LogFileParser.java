package nl.itqaanconsulting.logscope.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFileParser {

    private static final Pattern STRUCTURED_LINE = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d{3})?)\\s+"
                    + "(?<level>TRACE|DEBUG|INFO|WARN|ERROR)\\s+"
                    + "\\[(?<service>[^]]+)]\\s+"
                    + "(?:\\[(?<correlation>[^]]+)])?\\s*"
                    + "(?<message>.*)$"
    );

    private static final Pattern SPRING_BOOT_LINE = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2}T?\\s?\\d{2}:\\d{2}:\\d{2}[.,]\\d{3}"
                    + "(?:[+-]\\d{2}:?\\d{2})?)\\s+"
                    + "(?<level>TRACE|DEBUG|INFO|WARN|ERROR)\\s+"
                    + "(?:\\d+\\s+)?---\\s+"
                    + "\\[(?<context>[^]]*)]\\s+"
                    + "(?:\\[(?<thread>[^]]*)]\\s+)?"
                    + "(?<logger>\\S+)\\s+:\\s+(?<message>.*)$"
    );

    public LogAnalysis parse(Path file) throws IOException {
        List<LogEntry> entries = new ArrayList<>();
        EntryBuilder current = null;

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Optional<LogEntry> parsed = tryParseLine(line);
                if (parsed.isPresent()) {
                    addCurrent(entries, current);
                    current = new EntryBuilder(parsed.get());
                } else if (current != null && isContinuation(line)) {
                    current.appendDetail(line);
                } else if (!line.isBlank()) {
                    addCurrent(entries, current);
                    current = new EntryBuilder(unknownEntry(line));
                }
            }
        }
        addCurrent(entries, current);

        int errors = countLevel(entries, "ERROR");
        int warnings = countLevel(entries, "WARN");
        int services = (int) entries.stream()
                .map(LogEntry::service)
                .filter(service -> !service.isBlank())
                .filter(service -> !"unknown".equals(service))
                .distinct()
                .count();

        return new LogAnalysis(List.copyOf(entries), errors, warnings, services);
    }

    LogEntry parseLine(String line) {
        return tryParseLine(line).orElseGet(() -> unknownEntry(line));
    }

    private Optional<LogEntry> tryParseLine(String line) {
        Matcher structured = STRUCTURED_LINE.matcher(line);
        if (structured.matches()) {
            return Optional.of(new LogEntry(
                    structured.group("timestamp"),
                    structured.group("level"),
                    structured.group("service"),
                    "",
                    "",
                    structured.group("message"),
                    valueOrEmpty(structured.group("correlation")),
                    ""
            ));
        }

        Matcher spring = SPRING_BOOT_LINE.matcher(line);
        if (spring.matches()) {
            String secondContext = valueOrEmpty(spring.group("thread"));
            String service = secondContext.isBlank() ? "application" : spring.group("context").strip();
            String thread = secondContext.isBlank() ? spring.group("context").strip() : secondContext.strip();
            return Optional.of(new LogEntry(
                    spring.group("timestamp"),
                    spring.group("level"),
                    service,
                    thread,
                    spring.group("logger"),
                    spring.group("message"),
                    "",
                    ""
            ));
        }

        return Optional.empty();
    }

    private boolean isContinuation(String line) {
        String stripped = line.stripLeading();
        return line.isBlank()
                || Character.isWhitespace(line.charAt(0))
                || stripped.startsWith("Caused by:")
                || stripped.startsWith("Suppressed:")
                || stripped.startsWith("...")
                || stripped.matches("^[\\w.$]+(?::.*)?$");
    }

    private LogEntry unknownEntry(String line) {
        return new LogEntry("", "UNKNOWN", "unknown", "", "", line, "", "");
    }

    private void addCurrent(List<LogEntry> entries, EntryBuilder current) {
        if (current != null) {
            entries.add(current.build());
        }
    }

    private int countLevel(List<LogEntry> entries, String level) {
        return (int) entries.stream()
                .filter(entry -> level.equals(entry.level()))
                .count();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class EntryBuilder {

        private final LogEntry entry;
        private final StringBuilder details = new StringBuilder();

        private EntryBuilder(LogEntry entry) {
            this.entry = entry;
        }

        private void appendDetail(String line) {
            if (!details.isEmpty()) {
                details.append(System.lineSeparator());
            }
            details.append(line);
        }

        private LogEntry build() {
            return new LogEntry(
                    entry.timestamp(),
                    entry.level(),
                    entry.service(),
                    entry.thread(),
                    entry.logger(),
                    entry.message(),
                    entry.correlationId(),
                    details.toString()
            );
        }
    }
}
