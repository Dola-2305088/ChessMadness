package com.example.chessmadness;

import java.util.ArrayList;
import java.util.List;

interface Movement {
    List<int[]> PawnMovement();
    List<int[]> KingMovement();
    List<int[]> RookMovement();
    List<int[]> BishopMovement();
    List<int[]> QueenMovement();
    List<int[]> KnightMovement();
    // Future: castle(), enPassant(), etc.
}

public class chessPieceMovement implements Movement {

    private static final int BOARD_SIZE = 8;

    private final Board board;
    private final int row;
    private final int col;
    private final ChessPiece piece;

    public chessPieceMovement(Board board, int row, int col) {
        this(board, row, col, board != null ? board.get(row, col) : null);
    }

    public chessPieceMovement(Board board, int row, int col, ChessPiece piece) {
        this.board = board;
        this.row = row;
        this.col = col;
        this.piece = piece;
    }

    private boolean isInside(int r, int c) {
        if (board != null) return board.isInside(r, c);
        return r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE;
    }

    private boolean isEmpty(int r, int c) {
        return board == null ? true : board.isEmpty(r, c);
    }

    private ChessPiece at(int r, int c) {
        return board == null ? null : board.get(r, c);
    }

    private boolean isEnemy(int r, int c) {
        if (piece == null) return true; // if we don't know our color, treat others as capturable in demo
        ChessPiece target = at(r, c);
        if (target == null) return false;
        return target.getColor() != piece.getColor();
    }

    // ===== Pawn =====
    // Supports:
    //  - 1-step forward if empty
    //  - 2-step from starting rank if both squares empty
    //  - diagonal captures if enemy present
    // No en passant / promotion here.
    @Override
    public List<int[]> PawnMovement() {
        List<int[]> moves = new ArrayList<>();
        if (piece == null) return moves;

        int dir = (piece.getColor() == ChessPiece.Color.WHITE) ? -1 : +1; // white up, black down
        int r1 = row + dir;

        // one step forward
        if (isInside(r1, col) && isEmpty(r1, col)) {
            moves.add(new int[]{r1, col});

            // two-step from start rank if clear
            boolean onStart = (piece.getColor() == ChessPiece.Color.WHITE && row == 6)
                    || (piece.getColor() == ChessPiece.Color.BLACK && row == 1);
            int r2 = row + 2 * dir;
            if (onStart && isInside(r2, col) && isEmpty(r2, col)) {
                moves.add(new int[]{r2, col});
            }
        }

        // diagonal captures
        int[] diagCols = {col - 1, col + 1};
        for (int dc : diagCols) {
            if (isInside(r1, dc) && !isEmpty(r1, dc) && isEnemy(r1, dc)) {
                moves.add(new int[]{r1, dc});
            }
        }

        return moves;
    }

    // ===== King =====
    @Override
    public List<int[]> KingMovement() {
        List<int[]> moves = new ArrayList<>();
        int[][] dirs = {
                {-1,-1},{-1,0},{-1,1},
                { 0,-1},{ 0,1},
                { 1,-1},{ 1,0},{ 1,1}
        };
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            if (!isInside(r, c)) continue;
            if (isEmpty(r, c) || isEnemy(r, c)) {
                moves.add(new int[]{r, c});
            }
        }
        // Castling not implemented
        return moves;
    }

    // ===== Rook (rays with blocking) =====
    @Override
    public List<int[]> RookMovement() {
        List<int[]> moves = new ArrayList<>();
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        addRayMoves(moves, dirs);
        return moves;
    }

    // ===== Bishop (rays with blocking) =====
    @Override
    public List<int[]> BishopMovement() {
        List<int[]> moves = new ArrayList<>();
        int[][] dirs = {{-1,-1},{-1,1},{1,-1},{1,1}};
        addRayMoves(moves, dirs);
        return moves;
    }

    // ===== Queen (rook + bishop rays) =====
    @Override
    public List<int[]> QueenMovement() {
        List<int[]> moves = new ArrayList<>();
        int[][] dirs = {
                {-1,0},{1,0},{0,-1},{0,1},
                {-1,-1},{-1,1},{1,-1},{1,1}
        };
        addRayMoves(moves, dirs);
        return moves;
    }

    // ===== Knight =====
    @Override
    public List<int[]> KnightMovement() {
        List<int[]> moves = new ArrayList<>();
        int[][] jumps = {
                {-2,-1},{-2,1},{2,-1},{2,1},
                {-1,-2},{1,-2},{-1,2},{1,2}
        };
        for (int[] j : jumps) {
            int r = row + j[0], c = col + j[1];
            if (!isInside(r, c)) continue;
            if (isEmpty(r, c) || isEnemy(r, c)) {
                moves.add(new int[]{r, c});
            }
        }
        return moves;
    }

    // ===== Helpers =====
    // Adds ray moves for rook/bishop/queen with blocking and single capture
    private void addRayMoves(List<int[]> moves, int[][] dirs) {
        for (int[] d : dirs) {
            int r = row, c = col;
            while (true) {
                r += d[0]; c += d[1];
                if (!isInside(r, c)) break;

                if (isEmpty(r, c)) {
                    moves.add(new int[]{r, c});
                } else {
                    if (isEnemy(r, c)) {
                        moves.add(new int[]{r, c}); // capture square
                    }
                    break; // stop on first occupied square
                }
            }
        }
    }
}
