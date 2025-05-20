package dk.easv.blsgn.intgrpbelsign.gui.controllers.login;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import dk.easv.blsgn.intgrpbelsign.model.UserModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;

public class LoginController {

    private final UserModel userModel = new UserModel(new UserManager());

    @FXML
    private FlowPane buttonContainer;
    @FXML
    private TextField passwordField;
    @FXML
    private Label userNameLabel;

    public void onClickedBtnLogin(ActionEvent event) {
        String username = userNameLabel.getText();
        String password = passwordField.getText();
        try {
            User user = userModel.validateUser(username, password);
            if (user != null) {
                handleLogin(user);
            } else {
                System.out.println("Login failed. Invalid credentials.");
            }
        } catch (javax.naming.AuthenticationException e) {
            throw new RuntimeException(e);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLogin(User user) {
        String fxmlPath;
        String role;

        switch (user.getRole_id()) {
            case 1 -> {
                fxmlPath = "/dk/easv/blsgn/intgrpbelsign/Admin-dashboard.fxml";
                role = "Admin";
            }
            case 2 -> {
                fxmlPath = "/dk/easv/blsgn/intgrpbelsign/Qc.fxml";
                role = "Operator";
            }
            case 3 -> {
                fxmlPath = "/dk/easv/blsgn/intgrpbelsign/Operator-window.fxml";
                role = "Qc";
            }
            default -> {
                System.out.println("Login failed. Invalid role.");
                return;
            }
        }

        loadDashboard(fxmlPath, role);
    }

    private void loadDashboard(String fxmlPath, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            buttonContainer.getChildren().clear();
            buttonContainer.getChildren().add(root);

            System.out.println("Login successful for " + role + " user.");
        } catch (IOException e) {
            System.out.println("Failed to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUsername(String userName) {
        this.userNameLabel.setText(userName);
    }
}