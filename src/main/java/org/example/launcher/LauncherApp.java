package org.example.launcher;

import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LauncherApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage splashStage = new Stage(StageStyle.TRANSPARENT);
        Label splashLabel = new Label("null client");
        splashLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        splashLabel.setTextFill(Color.WHITE);
        StackPane splashRoot = new StackPane(splashLabel);
        splashRoot.setStyle("-fx-background-color: transparent;");
        Scene splashScene = new Scene(splashRoot, 400, 200);
        splashScene.setFill(Color.TRANSPARENT);
        splashStage.setScene(splashScene);
        splashStage.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> {
            splashStage.close();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/launcher/ui/fxml/main.fxml"));
                Scene mainScene = new Scene(loader.load(), 1000, 700);
                mainScene.setFill(Color.TRANSPARENT);
                mainScene.getStylesheets().add(getClass().getResource("/org/example/launcher/ui/css/material3.css").toExternalForm());

                primaryStage.initStyle(StageStyle.TRANSPARENT);
                primaryStage.setTitle("null client");
                primaryStage.setScene(mainScene);
                primaryStage.show();

                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), mainScene.getRoot());
                scaleTransition.setFromX(0.8);
                scaleTransition.setFromY(0.8);
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                scaleTransition.play();

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        delay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}