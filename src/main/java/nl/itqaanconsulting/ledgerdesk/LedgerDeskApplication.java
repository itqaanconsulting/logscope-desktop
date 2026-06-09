package nl.itqaanconsulting.ledgerdesk;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nl.itqaanconsulting.ledgerdesk.dashboard.DashboardView;

public class LedgerDeskApplication extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new DashboardView(), 1180, 760);
        scene.getStylesheets().add(
                LedgerDeskApplication.class.getResource("/styles/ledgerdesk.css").toExternalForm()
        );

        stage.setTitle("LedgerDesk");
        stage.setMinWidth(940);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
