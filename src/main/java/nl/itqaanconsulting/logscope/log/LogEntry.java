package nl.itqaanconsulting.logscope.log;

public record LogEntry(
        String timestamp,
        String level,
        String service,
        String thread,
        String logger,
        String message,
        String correlationId,
        String details
) {
    public boolean hasDetails() {
        return !details.isBlank();
    }
}
