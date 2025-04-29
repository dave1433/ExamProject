package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;

import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;



public class LoginController implements Initializable {


    @FXML
    private FlowPane buttonContainer;
    @FXML
    private Label roleLabel, mainName;

    private final UserManager userManager = new UserManager();

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
        List<User> users = userManager.getAllUsers();

        for (User user : users) {
            Button btn = new Button(user.getUser_name());
            btn.setPrefSize(210, 60);
            btn.setStyle("-fx-background-color: #BBDEFB;");
            btn.setOnAction(e -> handleUserClick(user.getUser_name()));

            InputStream imgStream = getClass().getResourceAsStream("/dk/easv/blsgn/intgrpbelsign/Pictures/icons/3.png");
            ImageView icon;

            if (imgStream != null) {
                icon = new ImageView(new Image(imgStream));
            } else {
                System.err.println("Icon not found");
                icon = new ImageView(); // fallback: create empty image view to avoid null
            }

            icon.setFitWidth(48);
            icon.setFitHeight(31);
            icon.setPreserveRatio(true);

            btn.setGraphic(icon);
            btn.setGraphicTextGap(10);
            btn.setAlignment(javafx.geometry.Pos.BASELINE_LEFT);

            FlowPane.setMargin(btn, new javafx.geometry.Insets(10, 10, 0, 10));

            buttonContainer.getChildren().add(btn);

            btn.setOnAction(event -> {
                if (user.getRole_id() == 1) {
                    try {
                        // Load LoginPassword.fxml
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/LoginPassword.fxml"));
                        Parent loginPasswordPane = loader.load();

                        LoginPassword loginPasswordController = loader.getController();
                        loginPasswordController.setUsername(user.getUser_name());

                        // Clear FlowPane and add the LoginPassword content
                        buttonContainer.getChildren().clear();
                        buttonContainer.getChildren().add(loginPasswordPane);

                        roleLabel.setText("Administrator");
                        mainName.setText(user.getUser_name());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (user.getRole_id() == 2) {
                    try{

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/LoginPassword.fxml"));
                        Parent loginPasswordPane = loader.load();

                        LoginPassword loginPasswordController = loader.getController();
                        loginPasswordController.setUsername(user.getUser_name());



                        buttonContainer.getChildren().clear();
                        buttonContainer.getChildren().add(loginPasswordPane);
                        roleLabel.setText("Quality Controller Department");
                        mainName.setText(user.getUser_name());


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (user.getRole_id() == 3) {

                    try{
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/Operator-window.fxml"));
                        Parent loginPasswordPane = loader.load();
                        System.out.println("LoginPassword loaded");

                        buttonContainer.getChildren().clear();
                        buttonContainer.getChildren().add(loginPasswordPane);
                        roleLabel.setText("Quality Controller Department");
                        mainName.setText(user.getUser_name());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    roleLabel.setText("OPERATOR");
                    mainName.setText(user.getUser_name());

                }
            });

        }


    }

    private void handleUserClick(String userName) {
        System.out.println("User clicked: " + userName);
    }

    public void onClickedBtn(ActionEvent event) {
    }
}

