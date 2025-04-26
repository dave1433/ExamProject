package dk.easv.blsgn.intgrpbelsign.gui;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginPassword {

    private final UserManager userManager = new UserManager();

    @FXML
    private TextField passwordField;
    @FXML
    private Label userNameLabel;

    public void onClickedBtnLogin(ActionEvent event) {

            String username = userNameLabel.getText();
            String password = passwordField.getText();

            User user = userManager.validateUser(username, password);
            if (user != null) {
                System.out.println("Login successful for user: " + user.getUser_name());
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/users-window.fxml"));
                    Parent root = loader.load();
                    Stage newStage = new Stage();
                    newStage.setTitle("BelSign");
                    newStage.setScene(new Scene(root));
                    newStage.show();
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


