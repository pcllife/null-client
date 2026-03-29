package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.launcher.GameLauncher;
import org.example.launcher.VersionManager;
import org.example.auth.AuthManager;

import java.io.File;
import java.util.List;

public class MainController {

    @FXML
    private ComboBox<String> versionCombo;

    @FXML
    private TextField usernameField;

    @FXML
    private Button launchButton;

    private VersionManager versionManager;
    private AuthManager authManager;

    @FXML
    public void initialize() {
        versionManager = new VersionManager();
        authManager = new AuthManager();

        // 加载可用版本列表（从官方版本清单或本地已安装）
        List<String> versions = versionManager.getAvailableVersions();
        versionCombo.getItems().addAll(versions);
        if (!versions.isEmpty()) {
            versionCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onLaunch() {
        String selectedVersion = versionCombo.getValue();
        String username = usernameField.getText().trim();

        if (selectedVersion == null || selectedVersion.isEmpty()) {
            showAlert("错误", "请选择一个游戏版本");
            return;
        }
        if (username.isEmpty()) {
            username = "Player"; // 默认名称
        }

        // 离线登录（实际可扩展微软登录）
        authManager.setOfflineUsername(username);

        // 启动游戏
        GameLauncher launcher = new GameLauncher(selectedVersion, authManager);
        try {
            launcher.launch();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("启动失败", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}