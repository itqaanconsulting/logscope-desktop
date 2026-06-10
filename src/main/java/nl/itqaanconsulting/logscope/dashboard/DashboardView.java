package nl.itqaanconsulting.logscope.dashboard;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import nl.itqaanconsulting.logscope.log.LogAnalysis;
import nl.itqaanconsulting.logscope.log.LogEntry;
import nl.itqaanconsulting.logscope.log.LogFileParser;
import nl.itqaanconsulting.logscope.log.LogFileSupport;

import java.io.File;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class DashboardView extends BorderPane {

    private final LogFileParser parser = new LogFileParser();
    private final ObservableList<LogEntry> entries = FXCollections.observableArrayList();
    private final FilteredList<LogEntry> filteredEntries = new FilteredList<>(entries);

    private final Label fileName = new Label("No log file selected");
    private final Label totalValue = metricValue("0", "total");
    private final Label errorValue = metricValue("0", "error");
    private final Label warningValue = metricValue("0", "warning");
    private final Label serviceValue = metricValue("0", "service");
    private final Label status = new Label("Open a .log file to start");
    private final TextField search = new TextField();
    private final CheckBox errorFilter = levelFilter("ERROR");
    private final CheckBox warningFilter = levelFilter("WARN");
    private final CheckBox infoFilter = levelFilter("INFO");
    private final CheckBox otherFilter = levelFilter("OTHER");
    private final Button openButton = new Button("Open log file");
    private final VBox dropZone = createDropZone();

    public DashboardView() {
        getStyleClass().add("app-shell");
        setTop(createHeader());
        setLeft(createNavigation());
        setCenter(createContent());
        configureFiltering();
        configureDragAndDrop();
    }

    private HBox createHeader() {
        Label brand = new Label("LogScope");
        brand.getStyleClass().add("brand");

        Label mode = new Label("DESKTOP");
        mode.getStyleClass().add("status-pill");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        openButton.getStyleClass().add("primary-button");
        openButton.setOnAction(event -> chooseLogFile());

        HBox header = new HBox(14, brand, mode, spacer, openButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        return header;
    }

    private VBox createNavigation() {
        Label section = new Label("ANALYSIS");
        section.getStyleClass().add("nav-caption");

        VBox navigation = new VBox(
                8,
                section,
                navItem("Log viewer", true),
                navItem("Timeline", false),
                navItem("Saved filters", false),
                navItem("Recent files", false)
        );
        navigation.getStyleClass().add("navigation");
        navigation.setPrefWidth(210);
        return navigation;
    }

    private Label navItem(String text, boolean active) {
        Label item = new Label(text);
        item.setMaxWidth(Double.MAX_VALUE);
        item.getStyleClass().add("nav-item");
        if (active) {
            item.getStyleClass().add("active");
        }
        return item;
    }

    private VBox createContent() {
        Label eyebrow = new Label("APPLICATION LOG");
        eyebrow.getStyleClass().add("eyebrow");

        fileName.getStyleClass().add("page-title");

        HBox metrics = new HBox(
                12,
                metricCard("Total lines", totalValue),
                metricCard("Errors", errorValue),
                metricCard("Warnings", warningValue),
                metricCard("Services", serviceValue)
        );
        metrics.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        search.setPromptText("Search message, service or correlation ID");
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        HBox filters = new HBox(12, search, errorFilter, warningFilter, infoFilter, otherFilter);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getStyleClass().add("filter-bar");

        TableView<LogEntry> logTable = createLogTable();
        VBox logPanel = panel("Log entries - double-click a row for details", status, logTable);
        VBox.setVgrow(logPanel, Priority.ALWAYS);

        VBox content = new VBox(18, eyebrow, fileName, dropZone, metrics, filters, logPanel);
        content.setPadding(new Insets(26));
        VBox.setVgrow(logPanel, Priority.ALWAYS);
        return content;
    }

    private VBox metricCard(String label, Label value) {
        Label caption = new Label(label);
        caption.getStyleClass().add("metric-label");

        VBox card = new VBox(8, caption, value);
        card.getStyleClass().add("metric-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox createDropZone() {
        Label title = new Label("Drop a log file here");
        title.getStyleClass().add("drop-title");

        Label formats = new Label("LOG, TXT, JSON, JSONL or NDJSON");
        formats.getStyleClass().add("drop-formats");

        VBox zone = new VBox(3, title, formats);
        zone.setAlignment(Pos.CENTER);
        zone.getStyleClass().add("drop-zone");
        return zone;
    }

    private Label metricValue(String text, String accentClass) {
        Label value = new Label(text);
        value.getStyleClass().addAll("metric-value", accentClass);
        return value;
    }

    private CheckBox levelFilter(String text) {
        CheckBox filter = new CheckBox(text);
        filter.setSelected(true);
        filter.getStyleClass().add("level-filter");
        return filter;
    }

    private VBox panel(String title, Label subtitle, javafx.scene.Node content) {
        Label heading = new Label(title);
        heading.getStyleClass().add("panel-title");
        subtitle.getStyleClass().add("panel-subtitle");

        VBox panel = new VBox(6, heading, subtitle, content);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    private TableView<LogEntry> createLogTable() {
        TableView<LogEntry> table = new TableView<>(filteredEntries);
        table.setPlaceholder(new Label("No log entries loaded"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<LogEntry, String> timestamp = column("Timestamp", LogEntry::timestamp);
        TableColumn<LogEntry, String> level = column("Level", LogEntry::level);
        TableColumn<LogEntry, String> service = column("Service", LogEntry::service);
        TableColumn<LogEntry, String> message = column("Message", LogEntry::message);
        TableColumn<LogEntry, String> correlation = column("Correlation ID", LogEntry::correlationId);

        timestamp.setPrefWidth(165);
        level.setPrefWidth(75);
        service.setPrefWidth(140);
        message.setPrefWidth(420);
        correlation.setPrefWidth(145);

        table.getColumns().add(timestamp);
        table.getColumns().add(level);
        table.getColumns().add(service);
        table.getColumns().add(message);
        table.getColumns().add(correlation);
        table.setRowFactory(view -> {
            TableRow<LogEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showDetails(row.getItem());
                }
            });
            return row;
        });
        return table;
    }

    private TableColumn<LogEntry, String> column(String title, Function<LogEntry, String> value) {
        TableColumn<LogEntry, String> column = new TableColumn<>(title);
        column.setCellValueFactory(entry -> new SimpleStringProperty(value.apply(entry.getValue())));
        return column;
    }

    private void configureFiltering() {
        search.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        errorFilter.selectedProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        warningFilter.selectedProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        infoFilter.selectedProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        otherFilter.selectedProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void configureDragAndDrop() {
        setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles() && LogFileSupport.firstSupported(dragboard.getFiles()).isPresent()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles()
                    && LogFileSupport.firstSupported(event.getDragboard().getFiles()).isPresent()) {
                dropZone.getStyleClass().add("drag-active");
            }
            event.consume();
        });

        setOnDragExited(event -> {
            dropZone.getStyleClass().remove("drag-active");
            event.consume();
        });

        setOnDragDropped(event -> {
            dropZone.getStyleClass().remove("drag-active");
            Optional<File> file = LogFileSupport.firstSupported(event.getDragboard().getFiles());
            file.ifPresentOrElse(
                    this::loadFile,
                    () -> status.setText("Unsupported file. Use LOG, TXT, JSON, JSONL or NDJSON.")
            );
            event.setDropCompleted(file.isPresent());
            event.consume();
        });
    }

    private void showDetails(LogEntry entry) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Log entry details");
        dialog.setHeaderText(entry.level() + " - " + entry.message());
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

        Label metadata = new Label(metadata(entry));
        metadata.getStyleClass().add("detail-metadata");

        TextArea details = new TextArea(fullDetails(entry));
        details.setEditable(false);
        details.setWrapText(false);
        details.getStyleClass().add("detail-text");
        details.setPrefColumnCount(100);
        details.setPrefRowCount(22);

        VBox content = new VBox(10, metadata, details);
        content.setPrefWidth(820);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private String metadata(LogEntry entry) {
        return "Timestamp: " + valueOrDash(entry.timestamp())
                + "    Service: " + valueOrDash(entry.service())
                + "    Thread: " + valueOrDash(entry.thread())
                + System.lineSeparator()
                + "Logger: " + valueOrDash(entry.logger())
                + "    Correlation ID: " + valueOrDash(entry.correlationId());
    }

    private String fullDetails(LogEntry entry) {
        if (!entry.hasDetails()) {
            return entry.message();
        }
        return entry.message() + System.lineSeparator() + entry.details();
    }

    private String valueOrDash(String value) {
        return value.isBlank() ? "-" : value;
    }

    private void applyFilters() {
        String query = search.getText().strip().toLowerCase(Locale.ROOT);
        filteredEntries.setPredicate(entry -> levelIsSelected(entry.level()) && matchesQuery(entry, query));
        updateStatus();
    }

    private boolean levelIsSelected(String level) {
        return switch (level) {
            case "ERROR" -> errorFilter.isSelected();
            case "WARN" -> warningFilter.isSelected();
            case "INFO" -> infoFilter.isSelected();
            default -> otherFilter.isSelected();
        };
    }

    private boolean matchesQuery(LogEntry entry, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return entry.message().toLowerCase(Locale.ROOT).contains(query)
                || entry.service().toLowerCase(Locale.ROOT).contains(query)
                || entry.correlationId().toLowerCase(Locale.ROOT).contains(query)
                || entry.thread().toLowerCase(Locale.ROOT).contains(query)
                || entry.logger().toLowerCase(Locale.ROOT).contains(query)
                || entry.details().toLowerCase(Locale.ROOT).contains(query);
    }

    private void chooseLogFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open log file");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Log files", "*.log", "*.txt", "*.json", "*.jsonl", "*.ndjson"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );

        File selected = chooser.showOpenDialog(getScene().getWindow());
        if (selected != null) {
            loadFile(selected);
        }
    }

    private void loadFile(File file) {
        openButton.setDisable(true);
        fileName.setText(file.getName());
        status.setText("Reading and parsing log file...");

        Task<LogAnalysis> task = new Task<>() {
            @Override
            protected LogAnalysis call() throws Exception {
                return parser.parse(file.toPath());
            }
        };

        task.setOnSucceeded(event -> {
            showAnalysis(task.getValue());
            openButton.setDisable(false);
        });
        task.setOnFailed(event -> {
            entries.clear();
            resetMetrics();
            status.setText("Could not read file: " + task.getException().getMessage());
            openButton.setDisable(false);
        });

        Thread thread = new Thread(task, "log-file-parser");
        thread.setDaemon(true);
        thread.start();
    }

    private void showAnalysis(LogAnalysis analysis) {
        entries.setAll(analysis.entries());
        totalValue.setText(format(analysis.totalLines()));
        errorValue.setText(format(analysis.errors()));
        warningValue.setText(format(analysis.warnings()));
        serviceValue.setText(format(analysis.services()));
        applyFilters();
    }

    private void resetMetrics() {
        totalValue.setText("0");
        errorValue.setText("0");
        warningValue.setText("0");
        serviceValue.setText("0");
    }

    private void updateStatus() {
        if (entries.isEmpty()) {
            status.setText("Open a .log file to start");
            return;
        }
        status.setText("Showing " + format(filteredEntries.size()) + " of " + format(entries.size()) + " entries");
    }

    private String format(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
