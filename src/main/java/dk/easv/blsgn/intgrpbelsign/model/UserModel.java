package dk.easv.blsgn.intgrpbelsign.model;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import javax.naming.AuthenticationException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserModel {
    private final UserManager userManager;
    private final ObservableList<User> users = FXCollections.observableArrayList();

    public UserModel(UserManager userManager) {
        this.userManager = userManager;
        refreshUsers();
    }
    public List<Button> generateUserButtons() {
        List<Button> buttons = new ArrayList<>();

        for (User user : users) {
            Button btn = new Button(user.getUser_name());
            btn.setPrefSize(210, 60);
            btn.setStyle("-fx-background-color: #BBDEFB;" + "-fx-background-radius: 8");

            InputStream imgStream = getClass().getResourceAsStream("/dk/easv/blsgn/intgrpbelsign/Pictures/icons/3.png");
            ImageView icon;

            if (imgStream != null) {
                icon = new ImageView(new Image(imgStream));
            } else {
                System.err.println("Icon not found");
                icon = new ImageView(); // fallback: create an empty image view to avoid null
            }

            icon.setFitWidth(48);
            icon.setFitHeight(31);
            icon.setPreserveRatio(true);

            btn.setGraphic(icon);
            btn.setGraphicTextGap(10);
            btn.setAlignment(Pos.BASELINE_LEFT);

            FlowPane.setMargin(btn, new javafx.geometry.Insets(10, 10, 0, 10));

            buttons.add(btn);
        }

        return buttons;
    }


    /**
     * Returns the observable list of users.
     * @return ObservableList of User objects.
     */
    public ObservableList<User> getUsers() {
        return users;
    }

    /**
     * Refreshes the list of users by fetching data from the UserManager.
     */
    public void refreshUsers() {
        users.setAll(userManager.getAllUsers());
    }

    /**
     * Retrieves all users as a List.
     * @return List of User objects.
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public User validateUser(String username, String password) throws AuthenticationException, javax.security.sasl.AuthenticationException {
        User user = userManager.validateUser(username, password);
        if (user == null) {
            throw new AuthenticationException("Invalid username or password");
        }
        return user;
    }

}