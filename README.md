# LogScope Desktop

Desktop log analysis application built with Java 21 and JavaFX.

## Current Version

LogScope can open plain-text log files asynchronously, parse structured entries and filter them by level or search term. Upcoming iterations add JSON log support and deeper analysis features.

## Run

```powershell
mvn javafx:run
```

Use `demo/sample-application.log` for a quick demonstration.

## Supported Log Format

```text
2026-06-09 10:42:18.413 ERROR [order-service] [req-91ac2] Payment failed
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
