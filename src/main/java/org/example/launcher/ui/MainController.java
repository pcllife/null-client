package org.example.launcher.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private HBox titleBar;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button closeButton;

    private double xOffset = 0;
    private double yOffset = 0;

    public void initialize() {
        welcomeLabel.setText("Welcome to null client");

        minimizeButton.setOnAction(event -> {
            Stage stage = (Stage) minimizeButton.getScene().getWindow();
            stage.setIconified(true);
        });

        closeButton.setOnAction(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        titleBar.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Stage stage = (Stage) titleBar.getScene().getWindow();
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
            }
        });

        titleBar.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Stage stage = (Stage) titleBar.getScene().getWindow();
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            }
        });
    }
}