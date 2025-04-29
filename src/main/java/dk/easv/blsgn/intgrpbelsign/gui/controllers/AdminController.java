package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminController {

    LoginController loginController = new LoginController();
    @FXML
    private FlowPane root; // Changed from buttonContainer to root
    @FXML
    private Label roleLabel, mainName;

    private dk.easv.blsgn.intgrpbelsign.be.User user;

    @FXML
    void onAddNewUser(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/users-window.fxml"));
            Parent newContent = loader.load();

            root.getChildren().clear();
            root.getChildren().add(newContent);

            Node sourceButton = (Node) event.getSource();
            sourceButton.setVisible(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}