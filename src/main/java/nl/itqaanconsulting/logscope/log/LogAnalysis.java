package nl.itqaanconsulting.logscope.log;

import java.util.List;

public record LogAnalysis(
        List<LogEntry> entries,
        int errors,
        int warnings,
        int services
) {
    public int totalLines() {
        return entries.size();
    }
}
