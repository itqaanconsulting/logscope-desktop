package nl.itqaanconsulting.logscope.dashboard;

public record DashboardSummary(
        int totalLines,
        int errors,
        int warnings,
        int services
) {
    public static DashboardSummary empty() {
        return new DashboardSummary(0, 0, 0, 0);
    }
}
