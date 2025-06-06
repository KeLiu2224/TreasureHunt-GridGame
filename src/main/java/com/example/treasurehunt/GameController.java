package com.example.treasurehunt;

import com.example.treasurehunt.models.*;
import com.example.treasurehunt.utils.PathSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.treasurehunt.models.GameModel.TREASURE_COUNT;
import static com.example.treasurehunt.models.GameModel.MAP_SIZE;

/**
 * A controller that handles game logic and user interactions for the Treasure Hunt game.
 * <p>
 * This class manages game initialization, player movement, obstacle generation,
 * treasure placement, and pathfinding functionality to provide hints to the player.
 * It follows the MVC design pattern as the controller component, connecting the
 * GameModel instance with the GameView instance.
 * </p>
 */
public class GameController {
    /** The data model for the game state */
    private final GameModel model;
    /** The view component that manages the display of the game */
    private GameView gameView;
    /** List of listeners that receive game event notifications from the GameController class*/
    private final List<GameListener> listeners = new ArrayList<>();

    /**
     * The constructor for the GameController class that
     * constructs a new GameController with an instance of the GameModel class.
     * @param model the game model object that stores variables about game states and other specific models,
     *              as well as methods such as getters and setters.
     */
    public GameController(GameModel model) {
        this.model = model;
    }

    /**
     * Sets the game view component for the controller object in the Main class.
     *
     * @param gameView the view component to display the game UI
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * Interface for objects that want to receive game update notifications.
     * This is implemented by the GamView class.
     */
    public interface GameListener {
        /**
         * Called when the user interface needs updates for message labels.
         *
         * @param message a descriptive update message
         */
        void onGameUpdated(String message);
    }

    /**
     * Adds a listener to the list of listeners to receive game update notifications.
     * Used in the constructor of the GameView class to add the GameView object.
     *
     * @param listener the listener to add, which is an object of type GameListener
     *                 or type of implementing classes.
     */
    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all registered listeners about a game update.
     *
     * @param message information about the update
     */
    private void notifyListeners(String message) {
        for (GameListener listener : listeners) {
            listener.onGameUpdated(message);
        }
    }

    /**
     * Initializes the game by setting or resetting the game model, generating obstacles,
     * and placing treasures on the map.
     */
    public void initGame() {
        model.reset();
        model.setNumOfObstacles(gameView.getObstacleNumChoice());
        generateObstacles();
        placeTreasures();
        notifyListeners("Game Started!");
    }

    /**
     * Randomly generates obstacles on the game map.
     * Places an obstacle only when a cell is empty and avoids placing an obstacle on the starting position (0,0).
     */
    private void generateObstacles() {
        Random random = new Random();
        int obstaclesPlaced = 0;

        while (obstaclesPlaced < GameModel.numberOfObstacles) {
            int x = random.nextInt(MAP_SIZE);
            int y = random.nextInt(MAP_SIZE);

            // Skip the starting position
            if (x == 0 && y == 0) continue;

            // Only add obstacles if cells are empty
            if (model.getMap()[y][x].getType() == Cell.CellType.EMPTY) {
                model.getMap()[y][x].setType(Cell.CellType.OBSTACLE);

                // If placing this obstacle would in enclosing the starting position with obstacles,
                // remove this obstacle from the current position and break the current iteration.
                if (!PathSolver.isAllCellsUnclosed(model.getMap(), 0, 0)) {
                    model.getMap()[y][x].setType(Cell.CellType.EMPTY);
                    continue;
                }

                obstaclesPlaced++;
            }
        }
    }

    /**
     * Places treasures randomly on the map, ensuring they are reachable by the player
     * and only placed on empty cells except the starting position.
     */
    private void placeTreasures() {
        model.clearTreasures();
        Random random = new Random();

        while (model.getTreasures().size() < TREASURE_COUNT) {
            int x = random.nextInt(GameModel.MAP_SIZE);
            int y = random.nextInt(GameModel.MAP_SIZE);

            // Avoid placing treasure on non-empty cells.
            if (model.getMap()[y][x].getType() != Cell.CellType.EMPTY || (x == 0 && y == 0)) continue;

            // Check whether treasures and only place a treasure if it is reachable from the starting position.
            if (isReachable(x, y)) {
                model.getTreasures().add(new int[]{x, y});
                model.getMap()[y][x].setType(Cell.CellType.TREASURE);
            }
        }
        // Initialize the number of remaining treasures
        model.setRemainingTreasures(model.getTreasures());
    }

    /**
     * Handles player movement in the specified direction.
     * <p>
     * This method updates the player position, handles collision detection with
     * boundaries and obstacles, tracks score deductions, and manages treasure
     * discovery events.
     * </p>
     *
     * @param dx horizontal movement (-1 for left, 1 for right, 0 for no horizontal movement)
     * @param dy vertical movement (-1 for up, 1 for down, 0 for no vertical movement)
     */
    public void movePlayer(int dx, int dy) {
        // Check if the game is over and perform no movement if it is true
        if (model.isGameOver()) return;

        // Check if the score is less than or equal to 0 and set GameOver to true if yes
        int score = model.getPlayer().getScore();
        if (score <= 0) {
            model.getPlayer().setScore(0);
            model.setGameOver(true);
            notifyListeners("Game over! You ran out of points.");
            return;
        }

        // Calculate the coordinates of the new position
        int newX = model.getPlayer().getPlayerX() + dx;
        int newY = model.getPlayer().getPlayerY() + dy;


        // Check if the new position is outside the map boundaries
        if (newX < 0 || newX >= MAP_SIZE || newY < 0 || newY >= MAP_SIZE) {
            notifyListeners("You hit the boundary! Try another direction.");
            return;
        }

        // Check if the new position is occupied by an obstacle
        if (model.getMap()[newY][newX].getType() == Cell.CellType.OBSTACLE) {
            model.getPlayer().hitObstacle();
            model.getMap()[newY][newX].reveal();

            // Update the score (-10) if bumping into an obstacle.
            // If the score is less or equal to 10, set the score to 0 and end the game.
            if (model.getPlayer().getScore() <= 10){
                model.getPlayer().setScore(0);
                model.setGameOver(true);
                notifyListeners("You hit an obstacle! -"+ score + " points. Score: " + model.getPlayer().getScore() + " Game over! You ran out of points.");
            } else {
                model.getPlayer().setScore(score - 10); // Decrease score by 10 for hitting an obstacle each time
                notifyListeners("You hit an obstacle! -10 points. Score: " + model.getPlayer().getScore());
            }

            return;
        }

        // Set the player position and update statistics for a valid move
        model.getPlayer().setPlayerX(newX);
        model.getPlayer().setPlayerY(newY);
        model.getPlayer().move();
        model.getPlayer().setScore(score - 1); // Decrease one score for each move

        // Mark the new cell as visited
        model.getPlayer().addVisitedCell(newX + "," + newY);

        // Check if the type of the new cell is treasure and is not yet revealed
        if (model.getMap()[newY][newX].getType() == Cell.CellType.TREASURE && !model.getMap()[newY][newX].isRevealed()) {
            model.findTreasure(newX, newY); // Update the number of treasures found
            model.getMap()[newY][newX].reveal();

            // Update the gameOver state if all treasures are found.
            if (model.getTreasuresFound() == TREASURE_COUNT) {
                model.setGameOver(true);
                notifyListeners("Congratulations! You found all treasures with a score of " + model.getPlayer().getScore() + "!");
            } else {
                notifyListeners("You found a treasure! " + (TREASURE_COUNT - model.getTreasuresFound()) + " remaining.");
            }
            return;
        }
        // End the game if the score becomes 0 after a moving into an empty cell (-1 point).
        if (model.getPlayer().getScore() == 0){
            model.setGameOver(true);
            notifyListeners("Game over! You ran out of points.");
        } else {
            notifyListeners("Player moved to (" + newX + ", " + newY + "). -1 point. Score: " + model.getPlayer().getScore());
        }
    }

    /**
     * Provides the player with a hint about the direction to move toward a treasure.
     * <p>
     * The hint shows the next step on the shortest path to the nearest treasure via the UI,
     * computed using the specified pathfinding algorithm. Using a hint costs the
     * player 3 points.
     * </p>
     *
     * @param algorithm the String name of the pathfinding algorithm to use ("A* Search" or "Breadth First Search")
     */
    public void getHint(String algorithm) {
        if (model.isGameOver()) return; // Stop getting a hint if the game is over
        int score = model.getPlayer().getScore();

        if (score < 3){ // Stop getting a hint if the score is less than 3
            notifyListeners("Not enough points for a hint! You need at least 3 points.");
            return;
        }
        if (score == 3){ // If the score is exact 3, set the score to 0 and end the game.
            model.getPlayer().setScore(0);
            model.setGameOver(true);
            notifyListeners("Game over! You ran out of points.");
            return;
        }

        if (model.getRemainingTreasures().isEmpty()) return;

        // Get the shortest path to the nearest treasure
        // using the default algorithm or the algorithm chosen by the user
        List<int[]> path = getPathToNearestTreasure(algorithm);

        // Only display the hint if the path (excluding the current position) is not empty.
        if (path.size() >= 1) {
            // Update the hint usage and the score, and display the next step in the path
            int[] nextStep = path.getFirst();
            if (gameView != null){
                model.getPlayer().useHint();
                model.getPlayer().setScore(score - 3); // Deduct 3 scores for each hint
                notifyListeners("Hint: Move to the highlighted cell (-3 points)");
                gameView.displayHint(nextStep[0], nextStep[1]);
            }

        } else {
            notifyListeners("No path found to any treasure!");
        }
    }

    /**
     * Call the specified algorithm to perform the path searching process
     * to find the shortest path to the nearest treasure, and returns the result.
     *
     * @param algorithm the String name of the pathfinding algorithm to use ("A* Search" or "Breadth First Search")
     * @return a list of coordinate pairs of two integers representing the path to the nearest treasure
     */
    public List<int[]> getPathToNearestTreasure(String algorithm){
        return switch (algorithm) {
            case "A* Search" ->
                    PathSolver.findShortestPathAStar(model.getMap(), model.getPlayer().getPlayerX(), model.getPlayer().getPlayerY(), model.getRemainingTreasures());
            case "Breadth First Search" ->
                    PathSolver.findShortestPathBFS(model.getMap(), model.getPlayer().getPlayerX(), model.getPlayer().getPlayerY(), model.getRemainingTreasures());
            default ->
                    PathSolver.findShortestPathAStar(model.getMap(), model.getPlayer().getPlayerX(), model.getPlayer().getPlayerY(), model.getRemainingTreasures());
        };
    }


    /**
     * Toggles transparent mode which reveals additional game information.
     */
    public void toggleTransparentMode() {
        model.setTransparentMode(!model.isTransparentMode());
        notifyListeners("Transparent mode " +
                (model.isTransparentMode() ? "activated" : "deactivated"));
    }

    /**
     * Determines if a cell at the specified coordinates is reachable from the player's current position.
     * <p>
     * Calls the A* search algorithm in the class PathSolver to find a path from the player to the position with the target coordinates,
     * and returns true if the returned path is not empty.
     * </p>
     *
     * @param targetX the x-coordinate of the target location
     * @param targetY the y-coordinate of the target location
     * @return true if the location is reachable, false otherwise
     */
    private boolean isReachable(int targetX, int targetY) {
        List<int[]> targets = new ArrayList<>();
        targets.add(new int[]{targetX, targetY});
        List<int[]> path = PathSolver.findShortestPathAStar(model.getMap(), model.getPlayer().getPlayerX(), model.getPlayer().getPlayerY(), targets);

        // If path is not empty, the location is reachable; otherwise no paths are found.
        return !path.isEmpty();
    }
}