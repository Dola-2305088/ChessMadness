module com.example.chessmadness {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.chessmadness to javafx.fxml;
    exports com.example.chessmadness;
}