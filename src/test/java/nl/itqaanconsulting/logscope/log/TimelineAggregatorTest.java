package nl.itqaanconsulting.logscope.log;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimelineAggregatorTest {

    private final TimelineAggregator aggregator = new TimelineAggregator();

    @Test
    void groupsErrorsAndWarningsByMinute() {
        List<TimelineBucket> buckets = aggregator.aggregate(List.of(
                entry("2026-06-09 10:42:18.413", "ERROR"),
                entry("2026-06-09 10:42:42.100", "ERROR"),
                entry("2026-06-09 10:42:55.000", "WARN"),
                entry("2026-06-09 10:43:01.000", "WARN")
        ));

        assertEquals(2, buckets.size());
        assertEquals(new TimelineBucket("10:42", 2, 1), buckets.getFirst());
        assertEquals(new TimelineBucket("10:43", 0, 1), buckets.getLast());
    }

    @Test
    void supportsOffsetAndCommaTimestamps() {
        List<TimelineBucket> buckets = aggregator.aggregate(List.of(
                entry("2026-06-09T11:03:13.441+02:00", "ERROR"),
                entry("2026-06-09 11:04:13,441", "WARN")
        ));

        assertEquals(new TimelineBucket("11:03", 1, 0), buckets.getFirst());
        assertEquals(new TimelineBucket("11:04", 0, 1), buckets.getLast());
    }

    @Test
    void ignoresInfoAndEntriesWithoutValidTimestamp() {
        List<TimelineBucket> buckets = aggregator.aggregate(List.of(
                entry("2026-06-09 10:42:18.413", "INFO"),
                entry("", "ERROR"),
                entry("not-a-date", "WARN")
        ));

        assertEquals(List.of(), buckets);
    }

    private LogEntry entry(String timestamp, String level) {
        return new LogEntry(timestamp, level, "service", "", "", "message", "", "");
    }
}
