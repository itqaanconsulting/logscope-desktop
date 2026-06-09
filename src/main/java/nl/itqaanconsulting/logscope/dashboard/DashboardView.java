package nl.itqaanconsulting.logscope.dashboard;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import nl.itqaanconsulting.logscope.log.LogEntry;

public class DashboardView extends BorderPane {

    public DashboardView() {
        getStyleClass().add("app-shell");
        setTop(createHeader());
        setLeft(createNavigation());
        setCenter(createContent(new DashboardSummary(12_486, 23, 117, 4)));
    }

    private HBox createHeader() {
        Label brand = new Label("LogScope");
        brand.getStyleClass().add("brand");

        Label mode = new Label("DESKTOP");
        mode.getStyleClass().add("status-pill");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button openButton = new Button("Open log file");
        openButton.getStyleClass().add("primary-button");

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

    private VBox createContent(DashboardSummary summary) {
        Label eyebrow = new Label("APPLICATION LOG");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("order-service.log");
        title.getStyleClass().add("page-title");

        HBox metrics = new HBox(
                12,
                metricCard("Total lines", summary.totalLines(), "total"),
                metricCard("Errors", summary.errors(), "error"),
                metricCard("Warnings", summary.warnings(), "warning"),
                metricCard("Services", summary.services(), "service")
        );
        metrics.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        TextField search = new TextField();
        search.setPromptText("Search message, service or correlation ID");
        search.getStyleClass().add("search-field");

        CheckBox error = levelFilter("ERROR", true);
        CheckBox warning = levelFilter("WARN", true);
        CheckBox info = levelFilter("INFO", true);
        HBox filters = new HBox(12, search, error, warning, info);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getStyleClass().add("filter-bar");

        TableView<LogEntry> logTable = createLogTable();
        VBox logPanel = panel(
                "Log entries",
                "Showing structured entries from the selected file",
                logTable
        );
        VBox.setVgrow(logPanel, Priority.ALWAYS);

        VBox content = new VBox(18, eyebrow, title, metrics, filters, logPanel);
        content.setPadding(new Insets(26));
        VBox.setVgrow(logPanel, Priority.ALWAYS);
        return content;
    }

    private VBox metricCard(String label, int value, String accentClass) {
        Label caption = new Label(label);
        caption.getStyleClass().add("metric-label");

        Label amount = new Label(String.format("%,d", value));
        amount.getStyleClass().addAll("metric-value", accentClass);

        VBox card = new VBox(8, caption, amount);
        card.getStyleClass().add("metric-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private CheckBox levelFilter(String text, boolean selected) {
        CheckBox filter = new CheckBox(text);
        filter.setSelected(selected);
        filter.getStyleClass().add("level-filter");
        return filter;
    }

    private VBox panel(String title, String subtitle, javafx.scene.Node content) {
        Label heading = new Label(title);
        heading.getStyleClass().add("panel-title");

        Label description = new Label(subtitle);
        description.getStyleClass().add("panel-subtitle");

        VBox panel = new VBox(6, heading, description, content);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    private TableView<LogEntry> createLogTable() {
        TableView<LogEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<LogEntry, String> timestamp = column("Timestamp", LogEntry::timestamp);
        TableColumn<LogEntry, String> level = column("Level", LogEntry::level);
        TableColumn<LogEntry, String> service = column("Service", LogEntry::service);
        TableColumn<LogEntry, String> message = column("Message", LogEntry::message);
        TableColumn<LogEntry, String> correlation = column("Correlation ID", LogEntry::correlationId);

        timestamp.setPrefWidth(150);
        level.setPrefWidth(75);
        service.setPrefWidth(130);
        message.setPrefWidth(420);
        correlation.setPrefWidth(145);

        table.getColumns().add(timestamp);
        table.getColumns().add(level);
        table.getColumns().add(service);
        table.getColumns().add(message);
        table.getColumns().add(correlation);
        table.setItems(FXCollections.observableArrayList(
                new LogEntry("10:42:18.413", "ERROR", "order-service",
                        "Payment provider returned HTTP 503", "req-91ac2"),
                new LogEntry("10:42:17.982", "WARN", "inventory-service",
                        "Stock reservation expires in 30 seconds", "req-91ac2"),
                new LogEntry("10:42:17.650", "INFO", "order-service",
                        "Order ORD-10482 validated", "req-91ac2"),
                new LogEntry("10:42:16.204", "INFO", "gateway",
                        "POST /api/orders accepted", "req-91ac2")
        ));
        return table;
    }

    private TableColumn<LogEntry, String> column(
            String title,
            java.util.function.Function<LogEntry, String> value
    ) {
        TableColumn<LogEntry, String> column = new TableColumn<>(title);
        column.setCellValueFactory(entry -> new SimpleStringProperty(value.apply(entry.getValue())));
        return column;
    }
}
