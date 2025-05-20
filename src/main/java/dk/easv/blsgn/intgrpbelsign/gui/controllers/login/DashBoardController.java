package dk.easv.blsgn.intgrpbelsign.gui.controllers.login;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import dk.easv.blsgn.intgrpbelsign.model.UserModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class DashBoardController implements Initializable {


    @FXML
    private FlowPane buttonContainer;
    @FXML
    private Label roleLabel, mainName;

    private final UserModel userModel = new UserModel(new UserManager());

    @FXML
    private TextField onSearchUsername;

    @FXML
    public void btnBackOnAction(ActionEvent event) {
        try {
            Parent mainRoot = FXMLLoader.load(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/MainLogin.fxml"));
            Scene mainScene = new Scene(mainRoot);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(mainScene);
            currentStage.setTitle("BelSign"); // Optional: set your main window title
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateUserButtons();
        for (var node : buttonContainer.getChildren()) {
            if (node instanceof Button) {
                allButtons.add((Button) node);
            }
        }

        // Listen for changes in the text field
        onSearchUsername.textProperty().addListener((obs, oldText, newText) -> {
            filterButtons(newText);
        });
    }


    // Store all buttons in a list so we can re-add them
    private final List<Button> allButtons = new ArrayList<>();


    private void filterButtons(String searchText) {
        // Save the search bar node
        Node searchBar = buttonContainer.getChildren().get(0);

        // Clear everything
        buttonContainer.getChildren().clear();

        // Re-add the search bar first
        buttonContainer.getChildren().add(searchBar);

        // Now add matching buttons
        for (Button btn : allButtons) {
            if (btn.getText().toLowerCase().contains(searchText.toLowerCase())) {
                buttonContainer.getChildren().add(btn);
            }
        }
    }


    private void populateUserButtons() {
        // Retrieve the list of buttons from the UserModel
        List<Button> buttons = userModel.generateUserButtons();

        // Add each button to the buttonContainer
        for (Button btn : buttons) {
            buttonContainer.getChildren().add(btn);
            allButtons.add(btn); // Store the button for filtering
        }

        // Add action handlers for each button
        for (Button btn : allButtons) {
            btn.setOnAction(event -> {
                String userName = btn.getText();
                User user = userModel.getUsers().stream()
                        .filter(u -> u.getUser_name().equals(userName))
                        .findFirst()
                        .orElse(null);

                if (user != null) {
                    handleUserAction(user);
                }
            });
        }
    }

    private void handleUserAction(User user) {
        try {
            FXMLLoader loader;
            Parent loginPane;

            switch (user.getRole_id()) {
                case 1 -> {
                    loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/LoginPassword.fxml"));
                    loginPane = loader.load();
                    LoginController loginController = loader.getController();
                    loginController.setUsername(user.getUser_name());
                    roleLabel.setText("Administrator");
                }
                case 2 -> {
                    loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/LoginPassword.fxml"));
                    loginPane = loader.load();
                    LoginController loginController = loader.getController();
                    loginController.setUsername(user.getUser_name());
                    roleLabel.setText("Quality Controller");
                }
                case 3 -> {
                    loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/OPLogin.fxml"));
                    loginPane = loader.load();
                    PINLogin pinLoginController = loader.getController();
                    pinLoginController.setUsername(user.getUser_name());
                    roleLabel.setText("Operator");
                }
                default -> throw new IllegalStateException("Unexpected role ID: " + user.getRole_id());
            }

            buttonContainer.getChildren().clear();
            buttonContainer.getChildren().add(loginPane);
            mainName.setText(user.getUser_name());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}