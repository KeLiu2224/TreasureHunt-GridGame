package com.example.treasurehunt.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The GameModel class represents the core game model of the Treasure Hunt game.
 * It maintains constants, variables for the map, player, treasures, obstacles and game states (game modes), and
 * getters and setters for these variables.
 */
public class GameModel {

    // Constant variables

    /** Size of the game map (for both width and height). */
    public static final int MAP_SIZE = 20;
    /** Size of each cell in pixels. */
    public static final int CELL_SIZE = 25;
    /** Default number of obstacles on the map. */
    public static final int DEFAULT_NUMBER_OF_OBSTACLES = 80;
    /** Number of treasures to find in the game. */
    public static final int TREASURE_COUNT = 3;
    /** Initial score/coins given to the player. */
    public static final int INITIAL_SCORE = 100;
    /** Default algorithm for giving hints and pathfinding. */
    public static final String DEFAULT_ALGORITHM = "A* Search";
    /** Starting position of the player. */
    public static final Position PLAYER_STARTING_POSITION = new Position(0, 0);
    /** Movement directions of the player: up, down, left, right. */
    public static final int[][] DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

    // Variables regarding the states of the game

    /** Boolean flag indicating if the game is over. */
    private boolean gameOver;
    /** Boolean flag indicating if transparent mode is active. */
    private boolean transparentMode;
    /** 2D array representing the game map. */
    private Cell[][] map;
    /** List of all treasure coordinates [x, y]. */
    private List<int[]> treasures;
    /** List of treasure coordinates not yet found. */
    private List<int[]> remainingTreasures;
    /** Number of treasures found by the player. */
    private int treasuresFound;
    /** Number of obstacles for the current run of the game. */
    public static int numberOfObstacles = DEFAULT_NUMBER_OF_OBSTACLES;
    /** The player object. */
    private final Player player = new Player(PLAYER_STARTING_POSITION.getX(), PLAYER_STARTING_POSITION.getY());


    /**
     * Constructor of the GameModel class.
     * Initializes variables, including the map, the list of treasures,
     * and the list of remaining treasures.
     */
    public GameModel() {
        this.map = new Cell[MAP_SIZE][MAP_SIZE];
        this.treasures = new ArrayList<>();
        this.remainingTreasures = new ArrayList<>();
    }

    /**
     * This method helps initialize and set the game to its initial state, and
     * is used by the method initGame() in the GameController class, for both
     * starting the game and restarting it.
     */
    public void reset() {
        treasuresFound = 0;
        gameOver = false;
        transparentMode = false;
        initializeMap();
        player.setPlayerX(0);
        player.setPlayerY(0);
        player.setScore(INITIAL_SCORE);
        player.resetMoves();
        player.resetHintsUsed();
        player.resetObstaclesHit();
        player.clearVisitedCells();
        player.addVisitedCell("0,0");
    }

    /**
     * Initializes the game map with empty cells.
     */
    private void initializeMap() {
        for (int y = 0; y < MAP_SIZE; y++) {
            for (int x = 0; x < MAP_SIZE; x++) {
                map[y][x] = new Cell(Cell.CellType.EMPTY);
            }
        }
    }

    // Getters and setters methods
    public Cell[][] getMap() { return map; }

    public Player getPlayer() { return player; }

    public List<int[]> getTreasures() { return treasures; }
    public int getTreasuresFound() { return treasuresFound; }
    public void findTreasure(int x, int y) {
        this.treasuresFound++;
        removeTreasureFromRemaining(x, y);
    }
    public void clearTreasures(){treasures.clear();}
    public void setNumOfObstacles(String percentage) {
        numberOfObstacles = switch (percentage) {
            case "10%" -> 40;
            case "20%" -> 80;
            case "30%" -> 120;
            default -> 80;
        };
    }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public boolean isTransparentMode() { return transparentMode; }
    public void setTransparentMode(boolean transparentMode) { this.transparentMode = transparentMode; }

    public List<int[]> getRemainingTreasures() {
        return remainingTreasures;
    }
    public void setRemainingTreasures(List<int[]> remainingTreasures) {this.remainingTreasures = remainingTreasures; }
    public void removeTreasureFromRemaining(int x, int y) {
        remainingTreasures.removeIf(treasure -> treasure[0] == x && treasure[1] == y);
    }

}
