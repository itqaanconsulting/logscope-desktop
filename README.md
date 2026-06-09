# LedgerDesk

Offline personal finance desktop application built with Java 21 and JavaFX.

## Current Version

The first version provides the desktop application shell and finance dashboard. Upcoming iterations add SQLite persistence, bank CSV import, categorization rules and budgets.

## Run

```powershell
mvn javafx:run
```

## Test

```powershell
mvn test
```

## Planned Scope

- Import bank transactions from CSV
- Categorize transactions and define automatic rules
- Track monthly budgets
- Visualize income, expenses and balance
- Store all data locally in SQLite
- Export reports and create local backups
- Package a Windows installer with `jpackage`
