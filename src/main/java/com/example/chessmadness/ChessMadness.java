package com.example.chessmadness;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ChessMadness extends Application {

    private static final int SIZE = 8;
    private static final int TILE_SIZE = 80;

    private final Board board = Board.standard();

    // UI references for the board
    private Rectangle[][] cellRects = new Rectangle[SIZE][SIZE];
    private StackPane[][] cellPanes = new StackPane[SIZE][SIZE];
    private ImageView[][] pieceNodes = new ImageView[SIZE][SIZE];

    // Selection state
    private Integer selectedRow = null;
    private Integer selectedCol = null;
    private List<int[]> selectedLegalMoves = new ArrayList<>();

    // Captures UI (panes where we place captured piece images)
    private FlowPane whiteCapturesPane; // pieces captured by White (i.e., Black pieces)
    private FlowPane blackCapturesPane; // pieces captured by Black (i.e., White pieces)
    private Label whiteScoreLabel;
    private Label blackScoreLabel;
    private int whiteScore = 0; // White's material score (sum of captured black piece values)
    private int blackScore = 0; // Black's material score (sum of captured white piece values)

    @Override
    public void start(Stage stage) {
        // Center: chess board
        GridPane boardGrid = buildBoardGrid();

        // Left/Right: capture panels + score
        VBox leftPanel = buildCapturePanel("White captures", true);
        VBox rightPanel = buildCapturePanel("Black captures", false);

        BorderPane root = new BorderPane();
        root.setCenter(boardGrid);
        root.setLeft(leftPanel);
        root.setRight(rightPanel);

        BorderPane.setAlignment(boardGrid, Pos.CENTER);

        Scene scene = new Scene(root, (SIZE * TILE_SIZE) + 280, (SIZE * TILE_SIZE) + 20);
        stage.setTitle("Chess — board, captures, and score");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane buildBoardGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Rectangle base = new Rectangle(TILE_SIZE, TILE_SIZE);
                base.setFill(((r + c) % 2 == 0) ? Color.BEIGE : Color.SADDLEBROWN);
                base.setStroke(Color.BLACK);
                base.setStrokeWidth(1);

                StackPane cell = new StackPane();
                cell.setPrefSize(TILE_SIZE, TILE_SIZE);
                cell.getChildren().add(base);

                cell.getProperties().put("r", r);
                cell.getProperties().put("c", c);

                ChessPiece p = board.get(r, c);
                if (p != null) {
                    ImageView iv = createPieceImageView(p);
                    cell.getChildren().add(iv);
                    pieceNodes[r][c] = iv;
                }

                cellRects[r][c] = base;
                cellPanes[r][c] = cell;

                cell.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> onCellClicked(cell));

                grid.add(cell, c, r);
            }
        }
        return grid;
    }

    private VBox buildCapturePanel(String title, boolean forWhite) {
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label scoreLbl = new Label("Score: 0");
        scoreLbl.setStyle("-fx-font-size: 14px;");

        FlowPane capturesPane = new FlowPane();
        capturesPane.setHgap(6);
        capturesPane.setVgap(6);
        capturesPane.setPrefWrapLength(120);
        capturesPane.setPadding(new Insets(8));
        capturesPane.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");

        VBox box = new VBox(8, titleLbl, scoreLbl, capturesPane);
        box.setPadding(new Insets(10));
        box.setPrefWidth(140);
        box.setAlignment(Pos.TOP_CENTER);

        if (forWhite) {
            whiteCapturesPane = capturesPane;
            whiteScoreLabel = scoreLbl;
        } else {
            blackCapturesPane = capturesPane;
            blackScoreLabel = scoreLbl;
        }
        return box;
    }

    private ImageView createPieceImageView(ChessPiece p) {
        Image img = new Image(getClass().getResourceAsStream(p.imagePath()));
        ImageView iv = new ImageView(img);
        iv.setFitWidth(TILE_SIZE * 0.8);
        iv.setFitHeight(TILE_SIZE * 0.8);
        iv.setPreserveRatio(true);
        iv.setMouseTransparent(true);
        return iv;
    }

    private ImageView createCapturedThumb(ChessPiece p) {
        Image img = new Image(getClass().getResourceAsStream(p.imagePath()));
        ImageView iv = new ImageView(img);
        iv.setFitWidth(36);
        iv.setFitHeight(36);
        iv.setPreserveRatio(true);
        return iv;
    }

    private void onCellClicked(StackPane cell) {
        int r = (int) cell.getProperties().get("r");
        int c = (int) cell.getProperties().get("c");

        if (selectedRow == null) {
            if (board.get(r, c) == null) { clearHighlights(); return; }
            selectSquare(r, c);
            return;
        }

        int fromR = selectedRow, fromC = selectedCol;

        if (containsMove(selectedLegalMoves, r, c)) {
            movePiece(fromR, fromC, r, c);
            clearSelection();
            clearHighlights();
            return;
        }

        clearHighlights();
        if (board.get(r, c) != null) selectSquare(r, c);
        else clearSelection();
    }

    private void selectSquare(int r, int c) {
        selectedRow = r;
        selectedCol = c;

        Rectangle origin = cellRects[r][c];
        origin.setStroke(Color.BLUE);
        origin.setStrokeWidth(3);

        ChessPiece p = board.get(r, c);
        selectedLegalMoves = computeMoves(p, r, c);

        for (int[] mv : selectedLegalMoves) {
            Rectangle tgt = cellRects[mv[0]][mv[1]];
            tgt.setStroke(Color.LIMEGREEN);
            tgt.setStrokeWidth(3);
        }
    }

    private void clearSelection() {
        selectedRow = null;
        selectedCol = null;
        selectedLegalMoves = new ArrayList<>();
    }

    private void movePiece(int fromR, int fromC, int toR, int toC) {
        ChessPiece moving = board.get(fromR, fromC);
        if (moving == null) return;

        // If destination has a piece, it's a capture
        ChessPiece captured = board.get(toR, toC);
        if (captured != null) {
            handleCapture(moving, captured, toR, toC);
        }

        // Update board data: move the piece
        board.set(toR, toC, moving);
        board.set(fromR, fromC, null);

        // Update UI: reparent the moving ImageView
        ImageView movingNode = pieceNodes[fromR][fromC];
        if (movingNode != null) {
            cellPanes[fromR][fromC].getChildren().remove(movingNode);
            cellPanes[toR][toC].getChildren().add(movingNode);
            pieceNodes[toR][toC] = movingNode;
            pieceNodes[fromR][fromC] = null;
        }
    }

    private void handleCapture(ChessPiece mover, ChessPiece captured, int toR, int toC) {
        // Remove captured node from the board cell
        ImageView capturedNode = pieceNodes[toR][toC];
        if (capturedNode != null) {
            cellPanes[toR][toC].getChildren().remove(capturedNode);
            pieceNodes[toR][toC] = null;
        }

        // Add a small thumbnail to the capturing side's panel and update score
        int val = pieceValue(captured.getType());
        if (mover.getColor() == ChessPiece.Color.WHITE) {
            whiteScore += val;
            whiteScoreLabel.setText("Score: " + whiteScore);
            whiteCapturesPane.getChildren().add(createCapturedThumb(captured));
        } else {
            blackScore += val;
            blackScoreLabel.setText("Score: " + blackScore);
            blackCapturesPane.getChildren().add(createCapturedThumb(captured));
        }
    }

    // Movement using your updated chessPieceMovement(board, r, c, piece)
    private List<int[]> computeMoves(ChessPiece piece, int r, int c) {
        if (piece == null) return new ArrayList<>();
        chessPieceMovement mv = new chessPieceMovement(board, r, c, piece);

        switch (piece.getType()) {
            case KNIGHT: return mv.KnightMovement();
            case QUEEN:  return mv.QueenMovement();
            case ROOK:   return mv.RookMovement();
            case BISHOP: return mv.BishopMovement();
            case KING:   return mv.KingMovement();
            case PAWN:   return mv.PawnMovement();
            default:     return new ArrayList<>();
        }
    }

    private boolean containsMove(List<int[]> moves, int r, int c) {
        for (int[] mv : moves) if (mv[0] == r && mv[1] == c) return true;
        return false;
    }

    private void clearHighlights() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Rectangle rect = cellRects[r][c];
                if (rect != null) {
                    rect.setStroke(Color.BLACK);
                    rect.setStrokeWidth(1);
                }
            }
        }
    }

    // Material values (common convention)
    private int pieceValue(ChessPiece.Type t) {
        switch (t) {
            case PAWN:   return 1;
            case KNIGHT: return 3;
            case BISHOP: return 3;
            case ROOK:   return 5;
            case QUEEN:  return 9;
            case KING:   return 0;
            default:     return 0;
        }
    }
}





//package com.example.chessmadness;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//
//import javafx.application.Application;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.StackPane;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.text.Font;
//import javafx.scene.text.Text;
//import javafx.stage.Stage;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ChessMadness extends Application {
//
//    private static final int SIZE = 8;
//    private static final int TILE_SIZE = 80;
//
//    private final Board board = Board.standard();
//
//    // UI references
//    private Rectangle[][] cellRects = new Rectangle[SIZE][SIZE];
//    private StackPane[][] cellPanes = new StackPane[SIZE][SIZE];
//    private ImageView[][] pieceNodes = new ImageView[SIZE][SIZE];
//
//    // Two-click selection
//    private Integer selectedRow = null;
//    private Integer selectedCol = null;
//    private List<int[]> selectedLegalMoves = new ArrayList<>();
//
//    @Override
//    public void start(Stage stage) {
//        GridPane grid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
//
//        for (int r = 0; r < SIZE; r++) {
//            for (int c = 0; c < SIZE; c++) {
//                // Base tile
//                Rectangle base = new Rectangle(TILE_SIZE, TILE_SIZE);
//                base.setFill(((r + c) % 2 == 0) ? Color.BEIGE : Color.SADDLEBROWN);
//                base.setStroke(Color.BLACK);
//                base.setStrokeWidth(1);
//
//                // Cell container
//                StackPane cell = new StackPane();
//                cell.setPrefSize(TILE_SIZE, TILE_SIZE);
//                cell.getChildren().add(base);
//
//                // Store coordinates on cell
//                cell.getProperties().put("r", r);
//                cell.getProperties().put("c", c);
//
//                // Add piece text if present
//                ChessPiece p = board.get(r, c);
//                if (p != null) {
//                    String path = p.imagePath();
//                    Image img = new Image(getClass().getResourceAsStream(path));
//                    ImageView iv = new ImageView(img);
//
//                    iv.setFitWidth(TILE_SIZE * 0.8);   // scale to 80% of tile
//                    iv.setFitHeight(TILE_SIZE * 0.8);
//                    iv.setPreserveRatio(true);
//                    iv.setMouseTransparent(true); // let clicks go through
//
//                    cell.getChildren().add(iv);
//                    pieceNodes[r][c] = iv; // <-- change pieceNodes to ImageView[][]
//                }
//
//                // Save refs
//                cellRects[r][c] = base;
//                cellPanes[r][c] = cell;
//
//                // Click handler on cell
//                cell.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> onCellClicked(cell));
//
//                grid.add(cell, c, r);
//            }
//        }
//
//        stage.setScene(new Scene(grid, SIZE * TILE_SIZE, SIZE * TILE_SIZE));
//        stage.setTitle("Chess — ChessPiece model + Movement");
//        stage.show();
//    }
//
//    private void onCellClicked(StackPane cell) {
//        int r = (int) cell.getProperties().get("r");
//        int c = (int) cell.getProperties().get("c");
//
//        if (selectedRow == null) {
//            if (board.get(r, c) == null) { clearHighlights(); return; }
//            selectSquare(r, c);
//            return;
//        }
//
//        int fromR = selectedRow, fromC = selectedCol;
//
//        if (containsMove(selectedLegalMoves, r, c)) {
//            movePiece(fromR, fromC, r, c);
//            clearSelection();
//            clearHighlights();
//            return;
//        }
//
//        clearHighlights();
//        if (board.get(r, c) != null) selectSquare(r, c);
//        else clearSelection();
//    }
//
//    private void selectSquare(int r, int c) {
//        selectedRow = r;
//        selectedCol = c;
//
//        // origin highlight
//        Rectangle origin = cellRects[r][c];
//        origin.setStroke(Color.BLUE);
//        origin.setStrokeWidth(3);
//
//        ChessPiece p = board.get(r, c);
//        selectedLegalMoves = computeMoves(p, r, c);
//
//
//        // targets highlight
//        for (int[] mv : selectedLegalMoves) {
//            Rectangle tgt = cellRects[mv[0]][mv[1]];
//            tgt.setStroke(Color.LIMEGREEN);
//            tgt.setStrokeWidth(3);
//        }
//    }
//
//    private void clearSelection() {
//        selectedRow = null;
//        selectedCol = null;
//        selectedLegalMoves = new ArrayList<>();
//    }
//
//    private void movePiece(int fromR, int fromC, int toR, int toC) {
//        ChessPiece moving = board.get(fromR, fromC);
//        if (moving == null) return;
//
//        // Update board data
//        board.set(toR, toC, moving);
//        board.set(fromR, fromC, null);
//
//        // Update UI nodes
//        ImageView movingNode = pieceNodes[fromR][fromC];
//        if (movingNode != null) {
//            // Remove captured node at destination (if any)
//            if (pieceNodes[toR][toC] != null) {
//                cellPanes[toR][toC].getChildren().remove(pieceNodes[toR][toC]);
//            }
//            // Reparent the moving node
//            cellPanes[fromR][fromC].getChildren().remove(movingNode);
//            cellPanes[toR][toC].getChildren().add(movingNode);
//
//            // Update references
//            pieceNodes[toR][toC] = movingNode;
//            pieceNodes[fromR][fromC] = null;
//        }
//    }
//
//    // Calls your chessPieceMovement (no blocking yet)
//    private List<int[]> computeMoves(ChessPiece piece, int r, int c) {
//        if (piece == null) return new ArrayList<>();
//        chessPieceMovement m = new chessPieceMovement(board,r, c,piece);
//
//        switch (piece.getType()) {
//            case KNIGHT: return m.KnightMovement();
//            case QUEEN:  return m.QueenMovement();
//            case ROOK:   return m.RookMovement();
//            case BISHOP: return m.BishopMovement();
//            case KING:   return m.KingMovement();
//            case PAWN:
//                // NOTE: your PawnMovement() always goes down (row+1).
//                // For demo, still call it. Next step: add direction to chessPieceMovement.
//                return m.PawnMovement();
//        }
//        return new ArrayList<>();
//    }
//
//    private boolean containsMove(List<int[]> moves, int r, int c) {
//        for (int[] mv : moves) if (mv[0] == r && mv[1] == c) return true;
//        return false;
//    }
//
//    private void clearHighlights() {
//        for (int r = 0; r < SIZE; r++) {
//            for (int c = 0; c < SIZE; c++) {
//                Rectangle rect = cellRects[r][c];
//                if (rect != null) {
//                    rect.setStroke(Color.BLACK);
//                    rect.setStrokeWidth(1);
//                }
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}

//package com.example.chessmadness;
//
//import javafx.application.Application;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.layout.GridPane;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Font;
//import javafx.scene.text.Text;
//import javafx.stage.Stage;
//
//public class HelloApplication extends Application {
//
//    private static final int SIZE = 8; // 8x8 chessboard
//    private static final int TILE_SIZE = 80;
//
//    // Unicode symbols for chess pieces
//    private final String[][] initialBoard = {
//            {"♜","♞","♝","♛","♚","♝","♞","♜"},
//            {"♟","♟","♟","♟","♟","♟","♟","♟"},
//            {"","","","","","","",""},
//            {"","","","","","","",""},
//            {"","","","","","","",""},
//            {"","","","","","","",""},
//            {"♙","♙","♙","♙","♙","♙","♙","♙"},
//            {"♖","♘","♗","♕","♔","♗","♘","♖"}
//    };
//
//    @Override
//    public void start(Stage primaryStage) {
//        GridPane grid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
//
//        for (int row = 0; row < SIZE; row++) {
//            for (int col = 0; col < SIZE; col++) {
//                // Create a square
//                javafx.scene.layout.StackPane square = new javafx.scene.layout.StackPane();
//                square.setPrefSize(TILE_SIZE, TILE_SIZE);
//
//                // Alternate colors
//                Color color = (row + col) % 2 == 0 ? Color.BEIGE : Color.SADDLEBROWN;
//                square.setStyle("-fx-background-color: " + toRgbCode(color) + ";");
//
//                // Add piece if exists
//                if (!initialBoard[row][col].isEmpty()) {
//                    Text piece = new Text(initialBoard[row][col]);
//                    piece.setFont(Font.font(36));
//                    square.getChildren().add(piece);
//                }
//
//                grid.add(square, col, row);
//            }
//        }
//
//        Scene scene = new Scene(grid, SIZE * TILE_SIZE, SIZE * TILE_SIZE);
//        primaryStage.setTitle("Chess Game");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    // Helper: convert Color to hex
//    private String toRgbCode(Color color) {
//        return String.format("#%02X%02X%02X",
//                (int)(color.getRed()*255),
//                (int)(color.getGreen()*255),
//                (int)(color.getBlue()*255));
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
