package nl.itqaanconsulting.logscope.log;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class TimelineAggregator {

    private static final DateTimeFormatter PLAIN_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter COMMA_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
    private static final DateTimeFormatter LABEL =
            DateTimeFormatter.ofPattern("HH:mm");

    public List<TimelineBucket> aggregate(List<LogEntry> entries) {
        Map<LocalDateTime, Counts> buckets = new TreeMap<>();

        entries.stream()
                .filter(entry -> "ERROR".equals(entry.level()) || "WARN".equals(entry.level()))
                .forEach(entry -> parseTimestamp(entry.timestamp()).ifPresent(timestamp -> {
                    LocalDateTime minute = timestamp.withSecond(0).withNano(0);
                    Counts counts = buckets.computeIfAbsent(minute, ignored -> new Counts());
                    if ("ERROR".equals(entry.level())) {
                        counts.errors++;
                    } else {
                        counts.warnings++;
                    }
                }));

        return buckets.entrySet().stream()
                .map(entry -> new TimelineBucket(
                        LABEL.format(entry.getKey()),
                        entry.getValue().errors,
                        entry.getValue().warnings
                ))
                .toList();
    }

    Optional<LocalDateTime> parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return Optional.empty();
        }

        for (DateTimeFormatter formatter : List.of(PLAIN_TIMESTAMP, COMMA_TIMESTAMP)) {
            try {
                return Optional.of(LocalDateTime.parse(timestamp, formatter));
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }

        try {
            return Optional.of(OffsetDateTime.parse(timestamp).toLocalDateTime());
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    private static final class Counts {
        private int errors;
        private int warnings;
    }
}
