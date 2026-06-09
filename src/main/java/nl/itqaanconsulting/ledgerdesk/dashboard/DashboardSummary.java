package nl.itqaanconsulting.ledgerdesk.dashboard;

import java.math.BigDecimal;

public record DashboardSummary(
        BigDecimal balance,
        BigDecimal income,
        BigDecimal expenses,
        int uncategorizedTransactions
) {
    public static DashboardSummary empty() {
        return new DashboardSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
    }
}
