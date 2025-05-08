package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;

public class PINLogin {

    @FXML
    private FlowPane buttonContainer;
    @FXML
    private TextField passwordField;
    @FXML
    private Label userNameLabel;

    @FXML
    private RadioButton rb1, rb2, rb3, rb4;

    private final UserManager userManager = new UserManager();
    @FXML
    public void initialize() {
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateRadioButtons(newVal.length());
                if (newVal.length() == 4) {
                    validateOperatorLogin();
                }
            });
        }
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

    private void validateOperatorLogin() {
        String username = userNameLabel.getText();
        String password = passwordField.getText();

        try {
            User user = userManager.validateUser(username, password);

            if (user == null) {
                indicateInvalidPassword();
                return;
            }

            if (user.getRole_id() == 3) {
                loadOperatorDashboard(); // You may need to update this if it needs an ActionEvent
            } else {
                indicateInvalidPassword();
            }
        } catch (AuthenticationException e) {
            indicateInvalidPassword();
        }
    }


    private void loadOperatorDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/Operator-window.fxml"));
            Parent root = loader.load();

            buttonContainer.getChildren().clear();
            buttonContainer.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void indicateInvalidPassword() {
        rb1.setStyle("-fx-mark-color: red;");
        rb2.setStyle("-fx-mark-color: red;");
        rb3.setStyle("-fx-mark-color: red;");
        rb4.setStyle("-fx-mark-color: red;");

        // Let JavaFX render the red styles *before* starting the timer
        Platform.runLater(() -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> resetPasswordField());
            pause.play();
        });
    }

    private void resetPasswordField() {
        passwordField.setText("");
        rb1.setStyle(""); // Reset to default
        rb2.setStyle("");
        rb3.setStyle("");
        rb4.setStyle("");

        updateRadioButtons(0);
    }

    public void setUserName(String userName) {
        this.userNameLabel.setText(userName);
    }


}
