# LogScope Desktop

Desktop log analysis application built with Java 21 and JavaFX.

## Current Version

The first version provides the application shell, log-level filters, search and a structured log table. Upcoming iterations add asynchronous file parsing, JSON log support and analysis features.

## Run

```powershell
mvn javafx:run
```

## Test

```powershell
mvn test
```

## Planned Scope

- Open large `.log` files without blocking the interface
- Filter by level, service, time and correlation ID
- Search plain-text and structured JSON logs
- Inspect stack traces and log details
- Visualize errors and warnings over time
- Export filtered results
- Package a Windows installer with `jpackage`
