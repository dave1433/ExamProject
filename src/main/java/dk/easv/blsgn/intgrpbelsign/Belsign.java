package dk.easv.blsgn.intgrpbelsign;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Belsign extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Belsign.class.getResource("MainLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Belsign");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {launch();
    }
}
