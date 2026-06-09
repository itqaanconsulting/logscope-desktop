package nl.itqaanconsulting.ledgerdesk.dashboard;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardSummaryTest {

    @Test
    void createsEmptySummary() {
        DashboardSummary summary = DashboardSummary.empty();

        assertEquals(BigDecimal.ZERO, summary.balance());
        assertEquals(BigDecimal.ZERO, summary.income());
        assertEquals(BigDecimal.ZERO, summary.expenses());
        assertEquals(0, summary.uncategorizedTransactions());
    }
}
