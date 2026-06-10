package nl.itqaanconsulting.logscope.log;

public record TimelineBucket(
        String label,
        int errors,
        int warnings
) {
}
