package nl.itqaanconsulting.ledgerdesk.dashboard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardView extends BorderPane {

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("nl-NL"));

    public DashboardView() {
        getStyleClass().add("app-shell");
        setTop(createHeader());
        setLeft(createNavigation());
        setCenter(createContent(DashboardSummary.empty()));
    }

    private HBox createHeader() {
        Label brand = new Label("LedgerDesk");
        brand.getStyleClass().add("brand");

        Label mode = new Label("OFFLINE");
        mode.getStyleClass().add("status-pill");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button importButton = new Button("Import CSV");
        importButton.getStyleClass().add("primary-button");

        HBox header = new HBox(14, brand, mode, spacer, importButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        return header;
    }

    private VBox createNavigation() {
        Label section = new Label("WORKSPACE");
        section.getStyleClass().add("nav-caption");

        VBox navigation = new VBox(
                8,
                section,
                navItem("Dashboard", true),
                navItem("Transactions", false),
                navItem("Budgets", false),
                navItem("Categories", false),
                navItem("Import history", false)
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
        Label eyebrow = new Label("FINANCIAL OVERVIEW");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        HBox metrics = new HBox(
                12,
                metricCard("Current balance", summary.balance(), "balance"),
                metricCard("Income this month", summary.income(), "income"),
                metricCard("Expenses this month", summary.expenses(), "expense"),
                metricCard("Needs attention", BigDecimal.valueOf(summary.uncategorizedTransactions()), "attention")
        );
        metrics.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        LineChart<String, Number> cashFlowChart = createCashFlowChart();
        VBox chartPanel = panel("Cash flow", "Income and expenses over the last six months", cashFlowChart);
        VBox.setVgrow(chartPanel, Priority.ALWAYS);

        TableView<Void> transactionTable = createTransactionTable();
        VBox transactionsPanel = panel(
                "Recent transactions",
                "Imported transactions will appear here",
                transactionTable
        );

        HBox lowerContent = new HBox(12, chartPanel, transactionsPanel);
        HBox.setHgrow(chartPanel, Priority.ALWAYS);
        HBox.setHgrow(transactionsPanel, Priority.ALWAYS);
        VBox.setVgrow(lowerContent, Priority.ALWAYS);

        VBox content = new VBox(18, eyebrow, title, metrics, lowerContent);
        content.setPadding(new Insets(26));
        return content;
    }

    private VBox metricCard(String label, BigDecimal value, String accentClass) {
        Label caption = new Label(label);
        caption.getStyleClass().add("metric-label");

        String formatted = "attention".equals(accentClass)
                ? value.toPlainString()
                : CURRENCY.format(value);
        Label amount = new Label(formatted);
        amount.getStyleClass().addAll("metric-value", accentClass);

        VBox card = new VBox(8, caption, amount);
        card.getStyleClass().add("metric-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
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

    private LineChart<String, Number> createCashFlowChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setMinHeight(280);
        return chart;
    }

    private TableView<Void> createTransactionTable() {
        TableView<Void> table = new TableView<>();
        table.setPlaceholder(new Label("Import a bank CSV to get started"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Void, String> date = new TableColumn<>("Date");
        TableColumn<Void, String> description = new TableColumn<>("Description");
        TableColumn<Void, String> amount = new TableColumn<>("Amount");
        table.getColumns().add(date);
        table.getColumns().add(description);
        table.getColumns().add(amount);
        return table;
    }
}
