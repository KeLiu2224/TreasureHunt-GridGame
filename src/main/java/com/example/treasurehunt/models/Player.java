package com.example.treasurehunt.models;

import java.util.HashSet;
import java.util.Set;

public class Player {
    private Position position;
    private int score;
    private int moves;
    private int hintsUsed = 0;
    private int obstaclesHit = 0;
    private final Set<String> visitedCells;

    /**
     * Constructor of the Player class.
     * Initializes the player's position and a hash set to keep track of visited cells.
     */
    public Player(){
        visitedCells = new HashSet<>();
    }
    public Player(int x, int y) {
        position = new Position(x, y);
        visitedCells = new HashSet<>();
    }


    // Getters and Setters of the player's properties.

    public int getPlayerX() {
        return position.getX();
    }
    public void setPlayerX(int playerX) {
        position.setX(playerX);
    }

    public int getPlayerY() {
        return position.getY();
    }
    public void setPlayerY(int playerY) {
        position.setY(playerY);
    }

    public void move(){moves++;}
    public int getMoves() {return moves;}
    public void resetMoves(){moves = 0;}

    public int getScore() {return score;}
    public void setScore(int score) {this.score = score;}

    public void useHint(){hintsUsed++;}
    public int getHintsUsed(){return hintsUsed;}
    public void resetHintsUsed(){hintsUsed = 0;}

    public void hitObstacle(){obstaclesHit++;}
    public int getObstaclesHit(){return obstaclesHit;}
    public void resetObstaclesHit(){obstaclesHit = 0;}



    public void addVisitedCell(String cellId) {
        visitedCells.add(cellId);
    }
    public boolean isCellVisited(String cellId) {
        return visitedCells.contains(cellId);
    }
    public void clearVisitedCells() {
        visitedCells.clear();
    }
}
