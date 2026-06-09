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

    @Test
    void parsesNestedJsonLogLine() {
        LogEntry entry = parser.parseLine("""
                {"@timestamp":"2026-06-09T11:03:13.441+02:00",\
                "log":{"level":"WARNING","logger":"nl.itqaan.StockClient"},\
                "service":{"name":"inventory-service"},\
                "process":{"thread":{"name":"http-nio-8081-exec-5"}},\
                "message":"Stock response was slow",\
                "trace":{"id":"7f31c98a"},\
                "exception":{"stacktrace":"java.net.ConnectException: timeout"}}
                """);

        assertEquals("2026-06-09T11:03:13.441+02:00", entry.timestamp());
        assertEquals("WARN", entry.level());
        assertEquals("inventory-service", entry.service());
        assertEquals("http-nio-8081-exec-5", entry.thread());
        assertEquals("nl.itqaan.StockClient", entry.logger());
        assertEquals("7f31c98a", entry.correlationId());
        assertEquals(true, entry.hasDetails());
    }

    @Test
    void supportsFlatJsonAliases() {
        LogEntry entry = parser.parseLine("""
                {"time":"2026-06-09T11:05:00Z","severity":"fatal","app":"gateway",\
                "thread_name":"worker-2","logger_name":"Gateway",\
                "msg":"Request failed","correlation_id":"req-22","stack_trace":"failure details"}
                """);

        assertEquals("ERROR", entry.level());
        assertEquals("gateway", entry.service());
        assertEquals("Request failed", entry.message());
        assertEquals("req-22", entry.correlationId());
        assertEquals("failure details", entry.details());
    }

    @Test
    void keepsMalformedJsonVisible() {
        LogEntry entry = parser.parseLine("{\"message\":\"incomplete\"");

        assertEquals("UNKNOWN", entry.level());
        assertEquals("{\"message\":\"incomplete\"", entry.message());
    }

    @Test
    void analyzesJsonLinesFile(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("application.jsonl");
        Files.writeString(file, String.join(System.lineSeparator(),
                "{\"level\":\"info\",\"service\":\"gateway\",\"message\":\"Request accepted\"}",
                "{\"level\":\"warn\",\"service\":\"orders\",\"message\":\"Slow response\"}",
                "{\"level\":\"error\",\"service\":\"orders\",\"message\":\"Request failed\"}"
        ));

        LogAnalysis analysis = parser.parse(file);

        assertEquals(3, analysis.totalLines());
        assertEquals(1, analysis.errors());
        assertEquals(1, analysis.warnings());
        assertEquals(2, analysis.services());
    }
}
