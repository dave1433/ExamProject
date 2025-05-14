package dk.easv.blsgn.intgrpbelsign.gui.controllers.admin;

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

        // Perform validation first
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        Role selectedRole = roleComboBox.getValue();

        // Basic validation
        if (username.isEmpty() || selectedRole == null || email.isEmpty() || password.isEmpty()) {
            showErrorDialog("Validation Error", "Username, Role, Email and Password are mandatory fields");
            return;
        }

        // For edit mode, only validate the password if it has been changed
        boolean isPasswordChanged = "add".equals(dialogType) || !password.equals(user.getPassword_hash());
        
        if (isPasswordChanged) {
            if (password.isEmpty()) {
                showErrorDialog("Validation Error", "Password is required");
                return;
            }

            // Check the password format for the Operator role
            if (selectedRole.getRole_id() == 3) {
                if (!Pattern.matches("^\\d{4}$", password)) {
                    showErrorDialog("Password Error", "Operator password must be exactly 4 numeric digits");
                    return;
                }
            }
        }

        // Email validation
        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            showErrorDialog("Validation Error", "Please enter a valid email address");
            return;
        }

        // Username existence check for new users
        if ("add".equals(dialogType) && userManager.doesUserNameExist(username)) {
            showErrorDialog("Validation Error", "Username already exists");
            return;
        }

        var saveTask = getBooleanTask(password, isPasswordChanged);

        new Thread(saveTask).start();
    }

    private Task<Boolean> getBooleanTask(String password, boolean isPasswordChanged) {
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
                    // Only pass the password if it was changed
                    success = userManager.editUser(user, isPasswordChanged ? password : null);
                }

                System.out.println((dialogType.equals("add") ? "addUser" : "editUser") + " result: " + success);
                return success;
            }
        };

        saveTask.setOnSucceeded(_ -> {
            if (saveTask.getValue()) {
                showInfoDialog("Success", dialogType.equals("add") ? "User created" : "User updated");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/Admin-dashboard.fxml"));
                    Parent adminView = loader.load();

                    AdminController adminController = loader.getController();

                    Node currentNode = saveButton;
                    while (currentNode.getParent() != null && !(currentNode instanceof FlowPane)) {
                        currentNode = currentNode.getParent();
                    }

                    if (currentNode instanceof FlowPane rootFlowPane) {
                        rootFlowPane.getChildren().clear();
                        rootFlowPane.getChildren().add(adminView);

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

        saveTask.setOnFailed(_ -> {
            showErrorDialog("Error", "An error occurred while saving the user");
            saveTask.getException().printStackTrace();
        });
        return saveTask;
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