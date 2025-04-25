package dk.easv.blsgn.intgrpbelsign.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginPassword {
    @FXML
    public void onClickedBtnLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/users-window.fxml")); // ✅ FIXED path
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("BelSign");
            newStage.setScene(new Scene(root)); // ✅ This line is good
            newStage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
