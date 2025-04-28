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
    private FlowPane buttonContainer;
    @FXML
    private Label roleLabel, mainName;

    private dk.easv.blsgn.intgrpbelsign.be.User user;



    @FXML
    void onAddNewUser(ActionEvent event){
        try {
            // Load LoginPassword.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/users-window.fxml"));
            Parent loginPasswordPane = loader.load();

            // Clear FlowPane and add the LoginPassword content
            buttonContainer.getChildren().clear();
            buttonContainer.getChildren().add(loginPasswordPane);

            roleLabel.setText("Administrator");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
