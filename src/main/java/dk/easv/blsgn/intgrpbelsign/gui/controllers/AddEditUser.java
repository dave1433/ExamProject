package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.Role;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;


import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class AddEditUser {

    @FXML
    private TextField usernameField, firstNameField, lastNameField, emailField, phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private Button saveButton;

    private final UserManager userManager = new UserManager();
    private User user;
    private String dialogType;

    public void initialize() {
        List<Role> roles = userManager.getAllRoles();
        roleComboBox.getItems().setAll(roles);
    }

    public void setDialogType(String dialogType) {
        this.dialogType = dialogType;
    }

    @FXML
    public void handleSave() {
        System.out.println("handleSave called");

        if (!isFormValid()) return;

        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() {
                boolean success = false;
                User user = createUserFromForm();

                if ("add".equals(dialogType)) {
                    System.out.println("Adding user");
                    success = userManager.addUser(user, password);
                } else if ("edit".equals(dialogType)) {
                    System.out.println("Editing user");
                    success = userManager.editUser(user, password);
                }

                System.out.println((dialogType.equals("add") ? "addUser" : "editUser") + " result: " + success);
                return success;
            }
        };

        saveTask.setOnSucceeded(e -> {
            if (saveTask.getValue()) {
                showInfoDialog("Success", dialogType.equals("add") ? "User created" : "User updated");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/Admin-dashboard.fxml"));
                    Parent adminView = loader.load();

                    // Get the controller and initialize it
                    AdminController adminController = loader.getController();

                    // Find the root FlowPane in the scene graph
                    Node currentNode = saveButton;
                    while (currentNode.getParent() != null && !(currentNode instanceof FlowPane)) {
                        currentNode = currentNode.getParent();
                    }

                    if (currentNode instanceof FlowPane) {
                        FlowPane rootFlowPane = (FlowPane) currentNode;
                        rootFlowPane.getChildren().clear();
                        rootFlowPane.getChildren().add(adminView);

                        // Initialize the controller after adding to the scene
                        if (adminController != null) {
                            adminController.initialize();
                        }
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                    showErrorDialog("Error", "Failed to return to admin view");
                }
            } else {
                showErrorDialog("Error", "Failed to save user");
            }
        });

        saveTask.setOnFailed(e -> {
            showErrorDialog("Error", "An error occurred while saving the user");
            saveTask.getException().printStackTrace();
        });

        new Thread(saveTask).start();
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            usernameField.setText(user.getUser_name());
            firstNameField.setText(user.getFirst_name());
            lastNameField.setText(user.getLast_name());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone_number());
            passwordField.setText(user.getPassword_hash());

            for (Role role : roleComboBox.getItems()) {
                if (role.getRole_id() == user.getRole_id()) {
                    roleComboBox.getSelectionModel().select(role);
                    break;
                }
            }
        }
    }

    private User createUserFromForm() {
        User user = "add".equals(dialogType) ? new User() : this.user;

        user.setUser_name(usernameField.getText());
        user.setFirst_name(firstNameField.getText());
        user.setLast_name(lastNameField.getText());
        user.setEmail(emailField.getText());
        user.setPhone_number(phoneField.getText());
        user.setRole_id(roleComboBox.getValue().getRole_id());
        return user;
    }

    private boolean isFormValid() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || roleComboBox.getValue() == null || email.isEmpty()) {
            showErrorDialog("Validation Error", "Username, Password, Rank and Email are mandatory fields");
            return false;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            showErrorDialog("Validation Error", "Please enter a valid email address");
            return false;
        }

        if ("add".equals(dialogType) && userManager.doesUserNameExist(username)) {
            showErrorDialog("Validation Error", "Username already exists");
            return false;
        }

        return true;
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}