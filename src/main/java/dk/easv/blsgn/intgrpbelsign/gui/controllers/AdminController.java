package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;


import java.io.IOException;
import java.util.List;

public class AdminController {
    @FXML
    private FlowPane root;
    @FXML
    private GridPane userGrid;

    private final UserManager userManager = new UserManager();
    private User selectedUser;

    @FXML
    public void initialize() {
        populateUsersGrid();
    }

    private void populateUsersGrid() {
        List<User> users = userManager.getAllUsers();

        // Clear the grid and its constraints
        userGrid.getChildren().clear();
        userGrid.getRowConstraints().clear();
        userGrid.setGridLinesVisible(false);
        userGrid.setGridLinesVisible(true);

        // Add header labels
        Label nameHeader = createHeaderLabel("Full Name");
        Label emailHeader = createHeaderLabel("E-mail");
        Label phoneHeader = createHeaderLabel("Phone Number");
        Label roleHeader = createHeaderLabel("Role");

        userGrid.add(new Label(""), 0, 0); // Empty label for icon column
        userGrid.add(nameHeader, 1, 0);
        userGrid.add(emailHeader, 2, 0);
        userGrid.add(phoneHeader, 3, 0);
        userGrid.add(roleHeader, 4, 0);

        int row = 1;
        for (User user : users) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(40);
            rowConstraints.setPrefHeight(40);
            userGrid.getRowConstraints().add(rowConstraints);

            // Create labels for user data
            Label nameLabel = createSelectableLabel(user.getFirst_name() + " " + user.getLast_name());
            Label emailLabel = createSelectableLabel(user.getEmail());
            Label phoneLabel = createSelectableLabel(user.getPhone_number());
            Label roleLabel = createSelectableLabel(user.getRole_id() == 1 ? "Admin" : user.getRole_id() == 2 ? "QC" : "Operator");

            // Create and configure user icon
            Label userIcon = new Label("");
            try {
                Image personIcon = new Image(getClass().getResourceAsStream("/dk/easv/blsgn/intgrpbelsign/Pictures/icons/person.png"));
                userIcon.setGraphic(new ImageView(personIcon));
            } catch (Exception e) {
                userIcon.setText("👤");
            }

            final int currentRow = row;

            // Add elements to the grid
            userGrid.add(userIcon, 0, row);
            userGrid.add(nameLabel, 1, row);
            userGrid.add(emailLabel, 2, row);
            userGrid.add(phoneLabel, 3, row);
            userGrid.add(roleLabel, 4, row);

            // Add click handlers
            Label[] rowLabels = {nameLabel, emailLabel, phoneLabel, roleLabel};
            for (Label label : rowLabels) {
                label.setOnMouseClicked(e -> {
                    selectedUser = user;
                    highlightRow(currentRow);
                });
            }

            row++;
        }
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setFont(new Font(14));
        label.setPadding(new Insets(5, 5, 5, 5));
        label.setStyle("-fx-font-weight: bold;");
        GridPane.setMargin(label, new Insets(10, 10, 10, 10));
        return label;
    }

    private Label createSelectableLabel(String text) {
        Label label = new Label(text);
        label.setFont(new Font(14));
        label.setPadding(new Insets(5, 5, 5, 5));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        GridPane.setMargin(label, new Insets(10, 10, 10, 10));
        return label;
    }

    private void highlightRow(int rowIndex) {
        userGrid.getChildren().forEach(node -> {
            if (node instanceof Label) {
                if (GridPane.getRowIndex(node) == 0) {
                    // Keep header style
                    node.getStyle();
                } else {
                    node.setStyle("-fx-background-color: transparent;");
                }
            }
        });

        userGrid.getChildren().forEach(node -> {
            if (node instanceof Label && GridPane.getRowIndex(node) != null
                    && GridPane.getRowIndex(node) == rowIndex) {
                if (GridPane.getRowIndex(node) == 0) {
                    // Keep header style while highlighted
                    node.getStyle();
                } else {
                    node.setStyle("-fx-background-color: #E3F2FD;");
                }
            }
        });
    }

    @FXML
    public void onEditUser(ActionEvent event) {
        if (selectedUser == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/create-edit-users.fxml"));
            Parent newContent = loader.load();

            // Get the controller and set it up for editing
            AddEditUser controller = loader.getController();
            controller.setDialogType("edit");
            controller.setUser(selectedUser);

            // Update the title and button text
            Label titleLabel = (Label) newContent.lookup("Label");
            if (titleLabel != null && titleLabel.getText().equals("Create User")) {
                titleLabel.setText("Edit User");
            }

            Button saveButton = (Button) newContent.lookup("#saveButton");
            if (saveButton != null) {
                saveButton.setText("Save Changes");
            }

            // Replace the content in the root FlowPane
            root.getChildren().clear();
            root.getChildren().add(newContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onAddNewUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/create-edit-users.fxml"));
            Parent newContent = loader.load();

            root.getChildren().clear();
            root.getChildren().add(newContent);

            AddEditUser dialogController = loader.getController();
            dialogController.setDialogType("add");

            Node sourceButton = (Node) event.getSource();
            sourceButton.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}