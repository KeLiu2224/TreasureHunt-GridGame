package com.example.treasurehunt;

import com.example.treasurehunt.models.GameModel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main application class for the Treasure Hunt game.
 * This class initializes the JavaFX application, sets up the MVC components,
 * and configures the primary stage.
 */
public class TreasureHuntApplication extends Application {
    /**
     * The start method for this JavaFX application.
     * Creates and connects the Model, View, and the Controller components,
     * configures the primary stage, and initializes the game state.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Create objects of MVC components
        GameModel model = new GameModel();
        GameController controller = new GameController(model);
        GameView view = new GameView(model, controller);

        // Connect the view object to the controller object
        controller.setGameView(view);

        // Set up the scene
        Scene scene = view.createGameScene();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Treasure Hunt");
        primaryStage.show();

        // Initialize game states, enable the initial game settings and generate the game map.
        controller.initGame();
    }

    public static void main(String[] args) {
        launch();
    }
}