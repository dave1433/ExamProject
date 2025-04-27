package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.Belsign;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class LoginController {
    @FXML
    private Label welcomeText;
    @FXML
    private VBox workerButtonsContainer;

    public void initialize() {
        List<String> workerNames = List.of("Alice", "Bob", "Charlie", "Diana", "Niels", "Daniel", "Rasmus"); // Dynamic data goes here

        for (String name : workerNames) {
            Button btn = new Button(name);
            btn.setPrefWidth(280); // Adjust as needed
            btn.setPrefHeight(44);
            btn.getStyleClass().add("button"); // inherits styles from CSS
            workerButtonsContainer.getChildren().add(btn);
        }
    }

    public void onLoginButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Belsign.class.getResource("Admin-dashboard.fxml"));
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace(); // handle more gracefully if needed
        }
    }

}