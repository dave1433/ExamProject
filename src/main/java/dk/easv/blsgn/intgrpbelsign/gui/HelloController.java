package dk.easv.blsgn.intgrpbelsign.gui;

import javafx.event.ActionEvent; // ✅ FIXED
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

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
    }

    @FXML
    private TextField onSearchUsername;

    @FXML
    private FlowPane buttonContainer;

    // Store all buttons in a list so we can re-add them
    private final List<Button> allButtons = new ArrayList<>();

    @FXML
    public void initialize() {
        // Store a copy of all buttons at startup
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

    private void filterButtons(String searchText) {
        buttonContainer.getChildren().clear();

        for (Button btn : allButtons) {
            if (btn.getText().toLowerCase().contains(searchText.toLowerCase())) {
                buttonContainer.getChildren().add(btn);
            }
        }
    }

}
