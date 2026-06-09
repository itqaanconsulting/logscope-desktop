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

    @Test
    void parsesSpringBootLogLine() {
        LogEntry entry = parser.parseLine(
                "2026-06-09T10:46:03.510+02:00 INFO 14228 --- "
                        + "[order-service] [nio-8080-exec-4] n.i.orders.OrderController : Order retrieved"
        );

        assertEquals("INFO", entry.level());
        assertEquals("order-service", entry.service());
        assertEquals("nio-8080-exec-4", entry.thread());
        assertEquals("n.i.orders.OrderController", entry.logger());
        assertEquals("Order retrieved", entry.message());
    }

    @Test
    void groupsStacktraceWithPreviousEntry(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("spring.log");
        Files.writeString(file, String.join(System.lineSeparator(),
                "2026-06-09T10:46:04.122+02:00 ERROR 14228 --- "
                        + "[order-service] [nio-8080-exec-5] n.i.OrderService : Request failed",
                "java.lang.IllegalStateException: invalid state",
                "\tat nl.itqaan.OrderService.process(OrderService.java:42)",
                "Caused by: java.net.ConnectException: Connection refused",
                "\tat java.base/sun.nio.ch.Net.pollConnect(Native Method)",
                "2026-06-09T10:46:05.001+02:00 INFO 14228 --- "
                        + "[order-service] [nio-8080-exec-5] n.i.OrderService : Request completed"
        ));

        LogAnalysis analysis = parser.parse(file);

        assertEquals(2, analysis.totalLines());
        assertEquals(1, analysis.errors());
        assertEquals(1, analysis.services());
        assertEquals(true, analysis.entries().getFirst().hasDetails());
        assertEquals(true, analysis.entries().getFirst().details().contains("Caused by:"));
    }
}
