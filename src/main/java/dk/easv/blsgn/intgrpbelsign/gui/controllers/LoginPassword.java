package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.io.IOException;

public class LoginPassword {

    private final UserManager userManager = new UserManager();
    @FXML
    private FlowPane buttonContainer;
    @FXML
    private TextField passwordField;
    @FXML
    private Label userNameLabel, roleLabel;

    public void onClickedBtnLogin(ActionEvent event) {

            String username = userNameLabel.getText();
            String password = passwordField.getText();

            User user = userManager.validateUser(username, password);

            if (user != null) {
                System.out.println("Login successful for user: " + user.getUser_name());
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/Admin-dashboard.fxml"));
                    Parent root = loader.load();

                    buttonContainer.getChildren().clear();
                    buttonContainer.getChildren().add(root);

                    Node sourceButton = (Node) event.getSource();
                    sourceButton.setVisible(false);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Login failed. Invalid username or password.");
            }

    }
    public void setUsername(String userName) {
        this.userNameLabel.setText(userName);
    }
}


