package dk.easv.blsgn.intgrpbelsign.gui;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;

import javafx.scene.image.ImageView;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

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
            Button btn = new Button(user.getUser_name());
            btn.setPrefSize(210, 60);
            btn.setStyle("-fx-background-color: #BBDEFB;");
            btn.setOnAction(e -> handleUserClick(user.getUser_name()));

            InputStream imgStream = getClass().getResourceAsStream("/dk/easv/blsgn/intgrpbelsign/Pictures/3.png");
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
        }
    }
    private void handleUserClick(String userName) {
        System.out.println("User clicked: " + userName);
    }

    public void onClickedBtn(ActionEvent event) {
    }
}
