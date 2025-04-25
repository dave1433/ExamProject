package dk.easv.blsgn.intgrpbelsign.gui;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import dk.easv.blsgn.intgrpbelsign.dal.UserDAO;
import javafx.event.ActionEvent; // ✅ FIXED
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private FlowPane buttonContainer;

    private final UserManager userManager = new UserManager();

    @FXML
    private TextField onSearchUsername;


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
        buttonContainer.getChildren().clear();

        for (Button btn : allButtons) {
            if (btn.getText().toLowerCase().contains(searchText.toLowerCase())) {
                buttonContainer.getChildren().add(btn);
            }
        }
    }


    private void populateUserButtons() {
        List<User> users = userManager.getAllUsers();

        for (User user : users) {
            Button btn = new Button(user.getUserName());
            btn.setPrefSize(210, 60);
            btn.setStyle("-fx-background-color: #BBDEFB;");
            btn.setOnAction(e -> handleUserClick(user.getUserName()));

           // ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("dk/easv/blsgn/intgrpbelsign/Pictures/3.png")));
            //icon.setFitWidth(48);
            //icon.setFitHeight(31);
           // icon.setPreserveRatio(true);

           // btn.setGraphic(icon);
            btn.setAlignment(javafx.geometry.Pos.BASELINE_LEFT);

            FlowPane.setMargin(btn, new javafx.geometry.Insets(10, 10, 0, 10));

            buttonContainer.getChildren().add(btn);
        }
    }

    private void handleUserClick(String userName) {
        System.out.println("User clicked: " + userName);
    }
}
/*
    @FXML
    public void onClickedBtn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/LoginPassword.fxml")); // ✅ FIXED path
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("BelSign");
            newStage.setScene(new Scene(root)); // ✅ This line is good
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/





