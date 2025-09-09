package com.example.chessmadness;

import com.example.networking.Client;
import javafx.application.Application;
import javafx.application.Platform;
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

    // Networking
    private Client client;
    private ChessPiece.Color myColor = null;
    private boolean canMove = false; // controlled by server

    // UI references
    private Rectangle[][] cellRects = new Rectangle[SIZE][SIZE];
    private StackPane[][] cellPanes = new StackPane[SIZE][SIZE];
    private ImageView[][] pieceNodes = new ImageView[SIZE][SIZE];

    // Selection state
    private Integer selectedRow = null;
    private Integer selectedCol = null;
    private List<int[]> selectedLegalMoves = new ArrayList<>();

    // Captures UI
    private FlowPane whiteCapturesPane;
    private FlowPane blackCapturesPane;
    private Label whiteScoreLabel;
    private Label blackScoreLabel;
    private int whiteScore = 0;
    private int blackScore = 0;

    @Override
    public void start(Stage stage) throws Exception {
        client = new Client(msg -> Platform.runLater(() -> handleServerMessage(msg)));
        client.connect("localhost", 5555);

        GridPane boardGrid = buildBoardGrid();
        VBox leftPanel = buildCapturePanel("White captures", true);
        VBox rightPanel = buildCapturePanel("Black captures", false);

        BorderPane root = new BorderPane();
        root.setCenter(boardGrid);
        root.setLeft(leftPanel);
        root.setRight(rightPanel);

        Scene scene = new Scene(root, (SIZE * TILE_SIZE) + 280, (SIZE * TILE_SIZE) + 20);
        stage.setTitle("Chess Multiplayer");
        stage.setScene(scene);
        stage.show();
    }

    private void handleServerMessage(String msg) {
        if (msg.equals("COLOR_WHITE")) {
            myColor = ChessPiece.Color.WHITE;
            canMove = true; // white starts
        } else if (msg.equals("COLOR_BLACK")) {
            myColor = ChessPiece.Color.BLACK;
            canMove = false; // black waits
        } else if (msg.equals("YOUR_TURN")) {
            canMove = true;
        } else if (msg.startsWith("MOVE:")) {
            String moveStr = msg.substring(5);
            handleOpponentMove(moveStr);
            canMove = false; // after opponent move, wait for YOUR_TURN
        }
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
        capturesPane.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd;");

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
        if (!canMove) return; // wait for server

        int r = (int) cell.getProperties().get("r");
        int c = (int) cell.getProperties().get("c");

        if (selectedRow == null) {
            if (board.get(r, c) == null) { clearHighlights(); return; }
            if (board.get(r, c).getColor() != myColor) return; // can't move opponent’s piece
            selectSquare(r, c);
            return;
        }

        int fromR = selectedRow, fromC = selectedCol;

        if (containsMove(selectedLegalMoves, r, c)) {
            movePiece(fromR, fromC, r, c);
            client.sendMove("MOVE:" + fromR + "," + fromC + "->" + r + "," + c);
            clearSelection();
            clearHighlights();
            canMove = false; // wait for server to allow next turn
            return;
        }

        clearHighlights();
        if (board.get(r, c) != null && board.get(r, c).getColor() == myColor)
            selectSquare(r, c);
        else clearSelection();
    }

    private void handleOpponentMove(String msg) {
        String[] parts = msg.split("->");
        String[] from = parts[0].split(",");
        String[] to = parts[1].split(",");
        int fromR = Integer.parseInt(from[0]);
        int fromC = Integer.parseInt(from[1]);
        int toR = Integer.parseInt(to[0]);
        int toC = Integer.parseInt(to[1]);

        movePiece(fromR, fromC, toR, toC);
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

        ChessPiece captured = board.get(toR, toC);
        if (captured != null) handleCapture(moving, captured, toR, toC);

        board.set(toR, toC, moving);
        board.set(fromR, fromC, null);

        ImageView movingNode = pieceNodes[fromR][fromC];
        if (movingNode != null) {
            cellPanes[fromR][fromC].getChildren().remove(movingNode);
            cellPanes[toR][toC].getChildren().add(movingNode);
            pieceNodes[toR][toC] = movingNode;
            pieceNodes[fromR][fromC] = null;
        }
    }

    private void handleCapture(ChessPiece mover, ChessPiece captured, int toR, int toC) {
        ImageView capturedNode = pieceNodes[toR][toC];
        if (capturedNode != null) {
            cellPanes[toR][toC].getChildren().remove(capturedNode);
            pieceNodes[toR][toC] = null;
        }

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
