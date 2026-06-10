# LogScope Desktop

Desktop log analysis application built with Java 21 and JavaFX.

## Current Version

LogScope can open or receive log files through drag-and-drop, parse them asynchronously and filter the resulting entries by level or search term. Plain-text, Spring Boot and JSON Lines formats are supported.

## Run

```powershell
mvn javafx:run
```

Use `demo/sample-application.log` or `demo/sample-structured.jsonl` for a quick demonstration.

## Supported Log Format

```text
2026-06-09 10:42:18.413 ERROR [order-service] [req-91ac2] Payment failed
```

Standard Spring Boot console logs are recognized as well. Double-click a table row to inspect its metadata and complete stacktrace.

JSON Lines fields from common structured logging formats are supported, including nested fields such as `log.level`, `service.name`, `trace.id` and `exception.stacktrace`.

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
