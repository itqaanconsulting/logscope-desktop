package nl.itqaanconsulting.logscope.log;

public record LogEntry(
        String timestamp,
        String level,
        String service,
        String message,
        String correlationId
) {
}
