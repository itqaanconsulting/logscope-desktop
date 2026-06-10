package nl.itqaanconsulting.logscope.log;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class LogFileSupport {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "log", "txt", "json", "jsonl", "ndjson"
    );

    private LogFileSupport() {
    }

    public static boolean isSupported(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }

        String name = file.getName();
        int extensionStart = name.lastIndexOf('.');
        if (extensionStart < 0 || extensionStart == name.length() - 1) {
            return false;
        }

        String extension = name.substring(extensionStart + 1).toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    public static Optional<File> firstSupported(List<File> files) {
        return files.stream()
                .filter(LogFileSupport::isSupported)
                .findFirst();
    }
}
