package com.example.chessmadness;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    private static final int SIZE = 8; // 8x8 chessboard
    private static final int TILE_SIZE = 80;

    // Unicode symbols for chess pieces
    private final String[][] initialBoard = {
            {"♜","♞","♝","♛","♚","♝","♞","♜"},
            {"♟","♟","♟","♟","♟","♟","♟","♟"},
            {"","","","","","","",""},
            {"","","","","","","",""},
            {"","","","","","","",""},
            {"","","","","","","",""},
            {"♙","♙","♙","♙","♙","♙","♙","♙"},
            {"♖","♘","♗","♕","♔","♗","♘","♖"}
    };

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // Create a square
                javafx.scene.layout.StackPane square = new javafx.scene.layout.StackPane();
                square.setPrefSize(TILE_SIZE, TILE_SIZE);

                // Alternate colors
                Color color = (row + col) % 2 == 0 ? Color.BEIGE : Color.SADDLEBROWN;
                square.setStyle("-fx-background-color: " + toRgbCode(color) + ";");

                // Add piece if exists
                if (!initialBoard[row][col].isEmpty()) {
                    Text piece = new Text(initialBoard[row][col]);
                    piece.setFont(Font.font(36));
                    square.getChildren().add(piece);
                }

                grid.add(square, col, row);
            }
        }

        Scene scene = new Scene(grid, SIZE * TILE_SIZE, SIZE * TILE_SIZE);
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Helper: convert Color to hex
    private String toRgbCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed()*255),
                (int)(color.getGreen()*255),
                (int)(color.getBlue()*255));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
