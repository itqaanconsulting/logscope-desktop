package nl.itqaanconsulting.logscope;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nl.itqaanconsulting.logscope.dashboard.DashboardView;

import java.io.File;

public class LogScopeApplication extends Application {

    @Override
    public void start(Stage stage) {
        File initialFile = getParameters().getUnnamed().stream()
                .map(File::new)
                .filter(File::isFile)
                .findFirst()
                .orElse(null);
        boolean showTimeline = getParameters().getRaw().contains("--view=timeline");

        Scene scene = new Scene(new DashboardView(initialFile, showTimeline), 1180, 760);
        scene.getStylesheets().add(
                LogScopeApplication.class.getResource("/styles/logscope.css").toExternalForm()
        );

        stage.setTitle("LogScope Desktop");
        stage.setMinWidth(940);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
