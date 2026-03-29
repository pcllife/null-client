module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    exports org.example;
    opens org.example.controller to javafx.fxml;
}