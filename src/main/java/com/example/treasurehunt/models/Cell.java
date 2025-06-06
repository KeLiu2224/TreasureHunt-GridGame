package com.example.treasurehunt.models;

public class Cell {
    public enum CellType {EMPTY, OBSTACLE, TREASURE} // Implicitly static and final

    private CellType type;
    private boolean revealed;

    public Cell(CellType type) {
        this.type = type;
        this.revealed = false;
    }

    public CellType getType() {
        return type;
    }
    public void setType(CellType type) {
        this.type = type;
    }

    public boolean isRevealed() {
        return revealed;
    }
    public void reveal() {
        this.revealed = true;
    }
}