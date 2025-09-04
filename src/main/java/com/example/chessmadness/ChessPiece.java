package com.example.chessmadness;

public class ChessPiece {
    public enum Type { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
    public enum Color { WHITE, BLACK }

    private final Type type;
    private final Color color;

    public ChessPiece(Type type, Color color) {
        this.type = type;
        this.color = color;
    }

    public Type getType() { return type; }
    public Color getColor() { return color; }

    public String imagePath() {
        String prefix = "/png/"; // folder inside resources
        String c = (color == Color.WHITE) ? "white" : "black";
        switch (type) {
            case KING:   return prefix +c + "-king"   + ".png";
            case QUEEN:  return prefix +c + "-queen"  + ".png";
            case ROOK:   return prefix +c + "-rook"   + ".png";
            case BISHOP: return prefix +c + "-bishop" +  ".png";
            case KNIGHT: return prefix +c + "-knight" + ".png";
            case PAWN:   return prefix +c + "-pawn"   +".png";
            default:     return null;
        }
    }
}
