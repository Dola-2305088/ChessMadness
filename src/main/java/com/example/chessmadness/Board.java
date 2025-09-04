package com.example.chessmadness;

public class Board {
    private final ChessPiece[][] cells = new ChessPiece[8][8];

    public ChessPiece get(int r, int c) { return cells[r][c]; }
    public void set(int r, int c, ChessPiece p) { cells[r][c] = p; }
    public boolean isInside(int r, int c) { return r>=0 && r<8 && c>=0 && c<8; }
    public boolean isEmpty(int r, int c) { return get(r,c) == null; }

    public static Board standard() {
        Board b = new Board();
        ChessPiece.Color W = ChessPiece.Color.WHITE;
        ChessPiece.Color B = ChessPiece.Color.BLACK;

        // Black back rank (row 0)
        b.set(0,0, new ChessPiece(ChessPiece.Type.ROOK,   B));
        b.set(0,1, new ChessPiece(ChessPiece.Type.KNIGHT, B));
        b.set(0,2, new ChessPiece(ChessPiece.Type.BISHOP, B));
        b.set(0,3, new ChessPiece(ChessPiece.Type.QUEEN,  B));
        b.set(0,4, new ChessPiece(ChessPiece.Type.KING,   B));
        b.set(0,5, new ChessPiece(ChessPiece.Type.BISHOP, B));
        b.set(0,6, new ChessPiece(ChessPiece.Type.KNIGHT, B));
        b.set(0,7, new ChessPiece(ChessPiece.Type.ROOK,   B));
        // Black pawns (row 1)
        for (int c = 0; c < 8; c++) b.set(1, c, new ChessPiece(ChessPiece.Type.PAWN, B));

        // Empty rows 2..5 (nulls by default)

        // White pawns (row 6)
        for (int c = 0; c < 8; c++) b.set(6, c, new ChessPiece(ChessPiece.Type.PAWN, W));
        // White back rank (row 7)
        b.set(7,0, new ChessPiece(ChessPiece.Type.ROOK,   W));
        b.set(7,1, new ChessPiece(ChessPiece.Type.KNIGHT, W));
        b.set(7,2, new ChessPiece(ChessPiece.Type.BISHOP, W));
        b.set(7,3, new ChessPiece(ChessPiece.Type.QUEEN,  W));
        b.set(7,4, new ChessPiece(ChessPiece.Type.KING,   W));
        b.set(7,5, new ChessPiece(ChessPiece.Type.BISHOP, W));
        b.set(7,6, new ChessPiece(ChessPiece.Type.KNIGHT, W));
        b.set(7,7, new ChessPiece(ChessPiece.Type.ROOK,   W));

        return b;
    }
}
