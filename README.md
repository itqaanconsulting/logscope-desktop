# LogScope Desktop

Desktop log analysis application built with Java 21 and JavaFX.

## Current Version

LogScope can open or receive log files through drag-and-drop, parse them asynchronously and filter the resulting entries by level or search term. A timeline highlights error and warning peaks per minute. Plain-text, Spring Boot and JSON Lines formats are supported.

The sidebar provides dedicated log viewer, timeline, saved filter and recent file views. Saved filters and recent files are retained for the current application session.

The current filtered result set can be exported as a UTF-8 CSV file for further analysis or sharing.

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

## Package For Windows

Create a standalone application containing its own Java runtime:

```powershell
.\scripts\package-windows.ps1
```

Packaging requires a full JDK 21 or newer with `jpackage`. The generated application itself does not require Java.

Start the packaged application with:

```powershell
.\target\dist\LogScope\LogScope.exe
```

An MSI can be built on a machine with WiX installed:

```powershell
.\scripts\package-windows.ps1 -Type msi
```

## Planned Scope

- Open large `.log` files without blocking the interface
- Filter by level, service, time and correlation ID
- Search plain-text and structured JSON logs
- Inspect stack traces and log details
