package com.example.chessmadness;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
                        // ekhono eta lagche na. default just.
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}