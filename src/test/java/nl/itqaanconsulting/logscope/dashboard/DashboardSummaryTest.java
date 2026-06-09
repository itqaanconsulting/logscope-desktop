package nl.itqaanconsulting.logscope.dashboard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardSummaryTest {

    @Test
    void createsEmptySummary() {
        DashboardSummary summary = DashboardSummary.empty();

        assertEquals(0, summary.totalLines());
        assertEquals(0, summary.errors());
        assertEquals(0, summary.warnings());
        assertEquals(0, summary.services());
    }
}
