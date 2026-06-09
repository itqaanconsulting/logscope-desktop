package nl.itqaanconsulting.logscope.log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LogFileParser {

    private static final Pattern LOG_LINE = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d{3})?)\\s+"
                    + "(?<level>TRACE|DEBUG|INFO|WARN|ERROR)\\s+"
                    + "\\[(?<service>[^]]+)]\\s+"
                    + "(?:\\[(?<correlation>[^]]+)])?\\s*"
                    + "(?<message>.*)$"
    );

    public LogAnalysis parse(Path file) throws IOException {
        List<LogEntry> entries;
        try (Stream<String> lines = Files.lines(file)) {
            entries = lines
                    .filter(line -> !line.isBlank())
                    .map(this::parseLine)
                    .toList();
        }

        int errors = countLevel(entries, "ERROR");
        int warnings = countLevel(entries, "WARN");
        int services = (int) entries.stream()
                .map(LogEntry::service)
                .filter(service -> !service.isBlank())
                .filter(service -> !"unknown".equals(service))
                .distinct()
                .count();

        return new LogAnalysis(entries, errors, warnings, services);
    }

    LogEntry parseLine(String line) {
        Matcher matcher = LOG_LINE.matcher(line);
        if (!matcher.matches()) {
            return new LogEntry("", "UNKNOWN", "unknown", line, "");
        }

        return new LogEntry(
                matcher.group("timestamp"),
                matcher.group("level"),
                matcher.group("service"),
                matcher.group("message"),
                valueOrEmpty(matcher.group("correlation"))
        );
    }

    private int countLevel(List<LogEntry> entries, String level) {
        return (int) entries.stream()
                .filter(entry -> level.equals(entry.level()))
                .count();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
