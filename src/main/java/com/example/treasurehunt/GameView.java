package com.example.treasurehunt;

import com.example.treasurehunt.models.Cell;
import com.example.treasurehunt.models.GameModel;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.List;

// Constants used by this class
import static com.example.treasurehunt.models.GameModel.CELL_SIZE;
import static com.example.treasurehunt.models.GameModel.MAP_SIZE;
import static com.example.treasurehunt.models.GameModel.TREASURE_COUNT;
import static com.example.treasurehunt.models.GameModel.DEFAULT_ALGORITHM;

public class GameView implements GameController.GameListener {
    private String algorithm = DEFAULT_ALGORITHM;

    private final GameModel model;
    private final GameController controller;

    // Declarations of some global UI objects
    private ComboBox<String> algorithmChoice;
    private ComboBox<String> obstacleNumChoice;
    private BorderPane root;
    private HBox centerBox;
    private VBox gameContainer;
    private GridPane gameGrid;
    private HBox controls;
    private VBox statsPanel;
    private VBox scoreBox, treasuresBox;
    private Label scoreLabel, treasuresFoundLabel, movesLabel, hintsUsedLabel, obstaclesHitLabel, messageLabel;

    /**
     * Constructor for the GameView class.
     * @param model an instance of the GameModel class
     * @param controller an instance of the GameController class
     */
    public GameView(GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.controller.addListener(this);
        createUI();
    }

    /**
     * Returns the selected percentage of obstacles that
     * the user to place on the map.
     * @return the selected percentage of obstacles
     */
    public String getObstacleNumChoice(){
        return obstacleNumChoice.getValue();
    }

    /**
     * Renders the game map once there is a change of the game state
     * informed by the method onGameUpdated() in the GameController class.
     */
    public void renderMap() {
        gameGrid.getChildren().clear();

        // Render map cells based on model state
        // Similar to original but reading from model
        for (int y = 0; y < MAP_SIZE; y++) {
            for (int x = 0; x < MAP_SIZE; x++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setStroke(Color.LIGHTGRAY);

                // Mark visited cells using light blue color
                if (model.getPlayer().isCellVisited(x + "," + y)) {
                    cell.setFill(Color.LIGHTBLUE);
                } else {
                    // Set border color for the boundary cells of the map
                    if (x == 0 || x == MAP_SIZE - 1 || y == 0 || y == MAP_SIZE - 1){
                        cell.setFill(Color.GHOSTWHITE);
                    }else {
                        cell.setFill(Color.WHITE);
                    }
                }


                // Display obstacles if revealed or in transparent testing mode
                if (model.getMap()[y][x].getType() == Cell.CellType.OBSTACLE && (model.getMap()[y][x].isRevealed() || model.isTransparentMode() || model.isGameOver())) {
                    cell.setFill(Color.GRAY);
                    gameGrid.add(cell, x, y);
                    continue;
                }

                // Display the player
                if (x == model.getPlayer().getPlayerX() && y == model.getPlayer().getPlayerY()) {
                    Circle player = new Circle(CELL_SIZE / 2.5);
                    player.setFill(Color.BLUE);
                    StackPane stack = new StackPane();
                    // If there's a treasure at the player's position, show it underneath the player
                    if (model.getMap()[y][x].getType() == Cell.CellType.TREASURE && (model.getMap()[y][x].isRevealed() || model.isTransparentMode() || model.isGameOver())) {
                        cell.setFill(Color.GOLD);
                    }
                    stack.getChildren().addAll(cell, player);
                    gameGrid.add(stack, x, y);
                    continue;
                }

                // Display treasures if revealed or in transparent testing mode
                if (model.getMap()[y][x].getType() == Cell.CellType.TREASURE && (model.getMap()[y][x].isRevealed() || model.isGameOver())) {
                    cell.setFill(Color.GOLD);
                    gameGrid.add(cell, x, y);
                    continue;
                }

                gameGrid.add(cell, x, y);

                if (model.isGameOver()){
                    // Set color for the cells that are not visited in the current solution path
                    List<int[]> path = controller.getPathToNearestTreasure(algorithm);
                    displayPath(path);
                }
            }
        }
    }

    /**
     * Displays a triangular hint inside a cell on the map given the specified coordinates.
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     */
    public void displayHint(int x, int y){
        Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
        cell.setFill(Color.LIGHTBLUE);
        cell.setStroke(Color.LIGHTGRAY);

        // Create an arrow to indicate the next step
        javafx.scene.shape.Polygon arrow = new javafx.scene.shape.Polygon();
        arrow.getPoints().addAll(
                Double.valueOf(CELL_SIZE/2.0), Double.valueOf(0.0),
                Double.valueOf(CELL_SIZE * 1.0), Double.valueOf(CELL_SIZE * 1.0),
                Double.valueOf(0.0), Double.valueOf(CELL_SIZE * 1.0)
        );
        arrow.setFill(Color.DARKBLUE);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(cell, arrow);
        gameGrid.add(stack, x, y);
        updateUI();
    }

    /**
     * Displays the solution path to the nearest remaining treasure on the map if any.
     * @param path a list of integer arrays consisting of x and y coordinates, which represents the path
     */
    public void displayPath(List<int[]> path){
        if (path == null || path.isEmpty()) return;

        // Get the total number of steps in the path
        int pathLength = path.size();

        for (int i = 0; i < pathLength - 1; i++) {
            int[] coordinates = path.get(i);
            int x = coordinates[0];
            int y = coordinates[1];

            // Create a colored cell to represent the path
            Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
            cell.setFill(Color.LIGHTGREEN); // Different color to distinguish from hints
            cell.setStroke(Color.DARKGREEN);

            // Add the cell to the grid
            gameGrid.add(cell, x, y);
        }

        // Add a triangular indicator for the first step if
        // the path contains more than the coordinates of the treasure
        if (!path.isEmpty() && pathLength > 1) {
            int[] firstStep = path.getFirst();
            displayHint(firstStep[0], firstStep[1]);
        }
    }

    /**
     * Updates the labels on the stats panel excepts the message label.
     * This method is called whenever the game state changes show the latest statistics of the game.
     */
    private void updateUI() {
        scoreLabel.setText(String.valueOf(model.getPlayer().getScore()));
        treasuresFoundLabel.setText(model.getTreasuresFound() + "/" + TREASURE_COUNT);
        movesLabel.setText(String.valueOf(model.getPlayer().getMoves()));
        hintsUsedLabel.setText(String.valueOf(model.getPlayer().getHintsUsed()));
        obstaclesHitLabel.setText(String.valueOf(model.getPlayer().getObstaclesHit()));
    }

    /**
     * This method implements the onGameUpdated method from the GameListener interface in the class GameController.
     * Updates the message of the message label in the stats panel, and calls methods updateUI and renderMap
     * when the game state changes.
     * @param message a message indicating the current game state
     */
    @Override
    public void onGameUpdated(String message) {
        if (message != null) {
            messageLabel.setText(message);
            messageLabel.setVisible(true);
        } else {
            messageLabel.setVisible(false);
        }

        updateUI();
        renderMap();
    }


    /**
     * Creates the game scene with the root pane and event handlers.
     * @return a Scene object representing the game scene
     */
    public Scene createGameScene() {
        Scene scene = new Scene(root, 800, 800);

        // Set up key event handlers
        scene.setOnKeyPressed(e -> {
            if (model.isGameOver()) return;

            switch (e.getCode()) {
                case UP: case W: controller.movePlayer(0, -1); break;
                case DOWN: case S: controller.movePlayer(0, 1); break;
                case LEFT: case A: controller.movePlayer(-1, 0); break;
                case RIGHT: case D: controller.movePlayer(1, 0); break;
                case H: algorithm = algorithmChoice.getValue();controller.getHint(algorithm); break;
                case BACK_QUOTE: controller.toggleTransparentMode(); break;
            }
        });

        return scene;
    }

    /**
     * Creates the UI components and layout for the game.
     * It is only used by the constructor of this class.
     */
    private void createUI() {
        // Create all UI components (moved from Main)

        // Build the main layout
        root = new BorderPane();
        root.setPadding(new Insets(10));

        // Title of the game
        Label title = new Label("Treasure Hunt");
        title.setFont(new Font(24));
        title.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        // Center Area - Game Map and Controls
        centerBox = new HBox(20);
        centerBox.setAlignment(Pos.CENTER);

        // Container for game map
        gameContainer = new VBox(10);
        gameContainer.setAlignment(Pos.CENTER);

        // Game grid
        gameGrid = new GridPane();
        gameGrid.setGridLinesVisible(true);

        // Game states panel
        statsPanel = new VBox(10);
        statsPanel.setPadding(new Insets(10));
        statsPanel.setStyle("-fx-background-color: #f3f4f6; -fx-border-radius: 5;");
        statsPanel.setPrefWidth(200);

        Label statsTitle = new Label("Game Stats");
        statsTitle.setFont(new Font(18));
        statsTitle.setStyle("-fx-font-weight: bold;");

        // Game Score
        scoreBox = new VBox(2);
        Label scoreText = new Label("Score:");
        scoreText.setStyle("-fx-text-fill: #6b7280;");
        scoreLabel = new Label(String.valueOf(model.getPlayer().getScore()));
        scoreLabel.setFont(new Font(20));
        scoreLabel.setStyle("-fx-font-weight: bold;");
        scoreBox.getChildren().addAll(scoreText, scoreLabel);

        // Found treasures
        treasuresBox = new VBox(2);
        Label treasuresText = new Label("Treasures Found:");
        treasuresText.setStyle("-fx-text-fill: #6b7280;");
        treasuresFoundLabel = new Label(model.getTreasuresFound() + "/" + TREASURE_COUNT);
        treasuresFoundLabel.setFont(new Font(20));
        treasuresFoundLabel.setStyle("-fx-font-weight: bold;");
        treasuresBox.getChildren().addAll(treasuresText, treasuresFoundLabel);

        // Number of moves
        VBox movesBox = new VBox(2);
        Label movesText = new Label("Moves:");
        movesText.setStyle("-fx-text-fill: #6b7280;");
        movesLabel = new Label(String.valueOf(model.getPlayer().getMoves()));
        movesLabel.setFont(new Font(20));
        movesLabel.setStyle("-fx-font-weight: bold;");
        movesBox.getChildren().addAll(movesText, movesLabel);

        // Number of hints used
        VBox hintsBox = new VBox(2);
        Label hintsText = new Label("Hints Used:");
        hintsText.setStyle("-fx-text-fill: #6b7280;");
        hintsUsedLabel = new Label(String.valueOf(model.getPlayer().getHintsUsed()));
        hintsUsedLabel.setFont(new Font(20));
        hintsUsedLabel.setStyle("-fx-font-weight: bold;");
        hintsBox.getChildren().addAll(hintsText, hintsUsedLabel);

        // Number of obstacles hit
        VBox obstaclesBox = new VBox(2);
        Label obstaclesText = new Label("Obstacles Hit Times:");
        obstaclesText.setStyle("-fx-text-fill: #6b7280;");
        obstaclesHitLabel = new Label(String.valueOf(model.getPlayer().getObstaclesHit()));
        obstaclesHitLabel.setFont(new Font(20));
        obstaclesHitLabel.setStyle("-fx-font-weight: bold;");
        obstaclesBox.getChildren().addAll(obstaclesText, obstaclesHitLabel);

        // Message label
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(5));
        messageLabel.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #f59e0b; -fx-border-width: 0 0 0 4;");
        messageLabel.setVisible(false);

        // Control buttons
        controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);

        // ComboBox and label for algorithm selection
        VBox choicesBox = new VBox(10);

        VBox algorithmBox = new VBox(5);
        algorithmBox.setAlignment(Pos.CENTER_LEFT);
        Label algorithmLabel = new Label("Select an algorithm:");
        algorithmChoice = new ComboBox<>();
        algorithmChoice.getItems().addAll("A* Search", "BFS Search");
        algorithmChoice.setValue("A* Search"); // Default algorithm
        algorithmChoice.setStyle("-fx-font-size: 12px;");

        VBox obstacleSettingBox = new VBox(5);
        obstacleSettingBox.setAlignment(Pos.CENTER_LEFT);
        Label obstacleNumChoiceLabel = new Label("Percentage of obstacles:");
        obstacleNumChoice = new ComboBox<>();
        obstacleNumChoice.getItems().addAll("10%", "20%", "30%");
        obstacleNumChoice.setValue("20%"); // Default number of obstacles
        obstacleNumChoice.setStyle("-fx-font-size: 12px;");

        // Function buttons
        VBox functionButtons = new VBox(10);
        functionButtons.setAlignment(Pos.CENTER);

        Button hintBtn = new Button("Hint (-3)");
        hintBtn.setOnAction(e -> {
            algorithm = algorithmChoice.getValue();
            controller.getHint(algorithm);
        });

        Button devBtn = new Button("Transparent Mode");
        devBtn.setOnAction(e -> controller.toggleTransparentMode());
        Button restartBtn = new Button("Restart");
        restartBtn.setOnAction(e -> {
            controller.initGame();
        });
        functionButtons.getChildren().addAll(hintBtn, devBtn, restartBtn);

        // Move buttons
        VBox moveButtons = new VBox(10);
        moveButtons.setAlignment(Pos.CENTER);
        // Up button
        Button upBtn = new Button("↑ Up");
        upBtn.setOnAction(e -> controller.movePlayer(0, -1));

        // Left and Right buttons
        HBox middleButtons = new HBox(10);
        middleButtons.setAlignment(Pos.CENTER);
        Button leftBtn = new Button("← Left");
        leftBtn.setOnAction(e -> controller.movePlayer(-1, 0));
        Button rightBtn = new Button("→ Right");
        rightBtn.setOnAction(e -> controller.movePlayer(1, 0));
        middleButtons.getChildren().addAll(leftBtn, rightBtn);

        // Down button
        Button downBtn = new Button("↓ Down");
        downBtn.setOnAction(e -> controller.movePlayer(0, 1));

        // Notice label for settings of algorithms and the number of obstacles
        VBox noticeBox = new VBox(5);
        noticeBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Note:");
        Label noticeLabel1 = new Label("(1) A changed algorithm choice takes effects in the next hint.");
        Label noticeLabel2 = new Label("(2) A new choice of obstacles percentage is applied after restarting the game.");

        // Add all buttons
        algorithmBox.getChildren().addAll(algorithmLabel, algorithmChoice);
        obstacleSettingBox.getChildren().addAll(obstacleNumChoiceLabel, obstacleNumChoice);
        choicesBox.getChildren().addAll(algorithmBox, obstacleSettingBox);
        moveButtons.getChildren().addAll(upBtn, middleButtons, downBtn);
        controls.getChildren().addAll(choicesBox, moveButtons, functionButtons);

        noticeBox.getChildren().addAll(titleLabel, noticeLabel1, noticeLabel2);

        // Add game map, controls and the noticeBox to the container
        gameContainer.getChildren().addAll(gameGrid, controls, noticeBox);
        // Add all statistics labels to the stats panel
        statsPanel.getChildren().addAll(statsTitle, scoreBox, treasuresBox, movesBox, hintsBox, obstaclesBox, messageLabel);
        // Add game container and stats panel to the center box
        centerBox.getChildren().addAll(gameContainer, statsPanel);
        root.setCenter(centerBox);
    }
}
