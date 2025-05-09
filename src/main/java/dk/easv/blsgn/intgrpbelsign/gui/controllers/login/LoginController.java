package dk.easv.blsgn.intgrpbelsign.gui.controllers.login;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;

public class LoginController {

    private final UserManager userManager = new UserManager();

    @FXML
    private FlowPane buttonContainer;
    @FXML
    private TextField passwordField;
    @FXML
    private Label userNameLabel;
    @FXML
    private RadioButton rb1, rb2, rb3, rb4;

    @FXML
    public void initialize() {
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateRadioButtons(newVal.length());
                if (newVal.length() == 4) {
                    validateAndLogin(newVal);
                }
            });
        }
    }

    private void validateAndLogin(String password) {
        try {
            User user = userManager.validateUser(userNameLabel.getText(), password);
            if (user != null) {
                handleLogin(user);
            } else {
                indicateInvalidPassword();
            }
        } catch (AuthenticationException e) {
            indicateInvalidPassword();
        }
    }

    public void onClickedBtnLogin(ActionEvent event) {
        String username = userNameLabel.getText();
        String password = passwordField.getText();
        try {
            User user = userManager.validateUser(username, password);
            if (user != null) {
                handleLogin(user);
            } else {
                indicateInvalidPassword();
            }
        } catch (AuthenticationException e) {
            indicateInvalidPassword();
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
            case 2, 3 -> {
                fxmlPath = "/dk/easv/blsgn/intgrpbelsign/Operator-window.fxml";
                role = "Operator";
            }
            default -> {
                System.out.println("Login failed. Invalid role.");
                indicateInvalidPassword();
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

    private void indicateInvalidPassword() {
        rb1.setStyle("-fx-mark-color: red;");
        rb2.setStyle("-fx-mark-color: red;");
        rb3.setStyle("-fx-mark-color: red;");
        rb4.setStyle("-fx-mark-color: red;");

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            passwordField.setStyle("");
            resetPasswordField();
        });
        pause.play();
    }

    private void resetPasswordField() {
        passwordField.setText("");
        rb1.setStyle("");
        rb2.setStyle("");
        rb3.setStyle("");
        rb4.setStyle("");
        updateRadioButtons(0);
    }

    private void updateRadioButtons(int length) {
        if (rb1 != null) rb1.setSelected(length >= 1);
        if (rb2 != null) rb2.setSelected(length >= 2);
        if (rb3 != null) rb3.setSelected(length >= 3);
        if (rb4 != null) rb4.setSelected(length >= 4);
    }

    @FXML
    private void handleKeypadPress(ActionEvent event) {
        Button btn = (Button) event.getSource();
        appendToPassword(btn.getText());
    }

    public void appendToPassword(String digit) {
        if (passwordField.getText().length() < 4) {
            passwordField.setText(passwordField.getText() + digit);
        }
    }

    public void setUsername(String userName) {
        this.userNameLabel.setText(userName);
    }
}