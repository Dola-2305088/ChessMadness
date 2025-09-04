package com.example.chessmadness;

import java.util.ArrayList;
import java.util.List;

interface Movement {
    void PawnMovement();
    void KingMovement();
    void RookMovement();
    void BishopMovement();
    void QueenMovement();
    void KnightMovement();
    //void castle();
    //void enpassant(); // ei gulo ekhono bujhtesi na kmne korbo.
}

public class chessPieceMovement implements Movement {

    private static final int BOARD_SIZE = 8;

    private int row;
    private int col;

    public chessPieceMovement(int row, int col) {
        this.row = row;
        this.col = col;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }
    private void printMoves(String piece, List<int[]> moves) {
        System.out.println(piece + " possible moves from (" + row + "," + col + "):");
        for (int[] move : moves) {
            System.out.println(" -> (" + move[0] + "," + move[1] + ")");
        }
    }

//    private boolean firstmovePawn = false;
//    private int count = 0;
    @Override
    public void PawnMovement() {
        List<int[]> moves = new ArrayList<>();
        int newRow = row + 1;
        int newCol = col + 0;

//        if(count >=8) firstmovePawn = true; // I am hell skeptical about this first 2 step pawn movement
//        if(!firstmovePawn)
//        {
//            newRow = row + 1;
//            count++;
//        }

        if(isValidPosition(newRow, newCol))
            moves.add(new int[]{newRow, newCol});
        printMoves("Pawn", moves);
    }

    @Override
    public void KingMovement() {
        List<int[]> moves = new ArrayList<>();

        // King can move one square in any direction
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1}, {0, -1},{0, 1}, {1, -1},{1, 0}, {1, 1}
        };

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (isValidPosition(newRow, newCol)) {
                moves.add(new int[]{newRow, newCol});
            }
        }
        printMoves("King", moves);
    }

    @Override
    public void RookMovement() {
        List<int[]> moves = new ArrayList<>();
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for( int[]dir : directions)
        {
            int newRow = row;
            int newCol = col;
            while(true)
            {
                newRow += dir[0];
                newCol += dir[1];
                if( !isValidPosition(newRow,newCol)) break;
                moves.add( new int[]{newRow, newCol});
            }
        }
        printMoves("Rook",moves);
    }

    @Override
    public void BishopMovement() {
        List<int[]> moves = new ArrayList<>();

        // Bishop moves diagonally (all four directions)
        int[][] directions = {
                {-1, -1}, {-1, 1},
                {1, -1}, {1, 1}
        };

        for (int[] dir : directions) {
            int newRow = row;
            int newCol = col;

            // Move until edge of board
            while (true) {
                newRow += dir[0];
                newCol += dir[1];

                if (!isValidPosition(newRow, newCol)) break;

                moves.add(new int[]{newRow, newCol});
            }
        }

        printMoves("Bishop", moves);
    }

    @Override
    public void QueenMovement() {
        List<int[]> moves = new ArrayList<>();
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for( int[]dir : directions)
        {
            int newRow = row;
            int newCol = col;
            while(true)
            {
                newRow += dir[0];
                newCol += dir[1];
                if( !isValidPosition(newRow,newCol)) break;
                moves.add( new int[]{newRow, newCol});
            }
        }
        printMoves("Queen",moves);
    }

    @Override
    public void KnightMovement() {
        List<int[]> moves = new ArrayList<>();
        int[][] directions = { {-2, -1}, {-2, 1}, {2, -1}, {2, 1},
                {-1, -2}, {1, -2}, {-1, 2}, {1, 2}};
        for( int[]dir : directions)
        {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if( isValidPosition(newRow,newCol))
            moves.add( new int[]{newRow, newCol});
        }
        printMoves("Knight",moves);
    }
            // this was just a checking code by gpt. ignore this.
//    public static void main(String[] args) {
//        chessPieceMovement pawn = new chessPieceMovement(2, 4);
//        pawn.PawnMovement();
//
//        chessPieceMovement king = new chessPieceMovement(4, 4);
//        king.KingMovement();
//
//        chessPieceMovement knight = new chessPieceMovement(4, 4);
//        knight.KnightMovement();
//
//        chessPieceMovement bishop = new chessPieceMovement(4, 4);
//        bishop.BishopMovement();
//
//        chessPieceMovement rook = new chessPieceMovement(4, 4);
//        rook.RookMovement();
//
//        chessPieceMovement queen = new chessPieceMovement(4, 4);
//        queen.QueenMovement();
//    }
}
