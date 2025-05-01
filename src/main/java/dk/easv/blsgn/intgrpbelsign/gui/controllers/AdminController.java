package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
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

    @FXML
    public void initialize() {
        // Add any necessary GridPane styling or constraints here
        populateUsersGrid();
    }

    @FXML
   public void onAddNewUser(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/users-window.fxml"));
            Parent newContent = loader.load();

            root.getChildren().clear();
            root.getChildren().add(newContent);

            // Get the controller of the dialog
            AddEditUser dialogController = loader.getController();
            dialogController.setDialogType("add");


            Node sourceButton = (Node) event.getSource();
            sourceButton.setVisible(false);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void populateUsersGrid() {
        List<User> users = userManager.getAllUsers();

        // Clear the grid and its constraints
        userGrid.getChildren().clear();
        userGrid.getRowConstraints().clear();
        userGrid.setGridLinesVisible(false);
        userGrid.setGridLinesVisible(true);

        // Create header labels with proper styling
        Label nameHeader = new Label("Full Name");
        Label emailHeader = new Label("E-mail");
        Label phoneHeader = new Label("Phone Number");
        Label roleHeader = new Label("Role");

        // Add consistent padding and margin to headers
        Insets padding = new Insets(5, 5, 5, 5);
        Insets margin = new Insets(10, 10, 10, 10);

        // Apply styling to header labels
        for (Label header : new Label[]{nameHeader, emailHeader, phoneHeader, roleHeader}) {
            header.setPadding(padding);
            GridPane.setMargin(header, margin);
        }

        // Add headers to the grid
        userGrid.add(new Label(""), 0, 0); // Empty label for icon column
        userGrid.add(nameHeader, 1, 0);
        userGrid.add(emailHeader, 2, 0);
        userGrid.add(phoneHeader, 3, 0);
        userGrid.add(roleHeader, 4, 0);

        int row = 1;
        for (User user : users) {
            // Add row constraints
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(40);
            rowConstraints.setPrefHeight(40);
            userGrid.getRowConstraints().add(rowConstraints);

            // Create labels for user data
            Label nameLabel = new Label(user.getFirst_name() + " " + user.getLast_name());
            Label emailLabel = new Label(user.getEmail());
            Label phoneLabel = new Label(user.getPhone_number());


            // Apply padding and margin to data labels
            for (Label label : new Label[]{nameLabel, emailLabel, phoneLabel}) {
                label.setPadding(padding);
                GridPane.setMargin(label, margin);
            }

            // Create and configure user icon
            Label userIcon = new Label("");
            try {
                Image personIcon = new Image(getClass().getResourceAsStream("/dk/easv/blsgn/intgrpbelsign/Pictures/icons/person.png"));
                userIcon.setGraphic(new ImageView(personIcon));
            } catch (Exception e) {
                userIcon.setText("👤");
            }

            // Create and configure role label
            Label roleLabel = new Label(user.getRole_id() == 1 ? "Admin" : "QC");
            roleLabel.setPadding(padding);
            GridPane.setMargin(roleLabel, margin);
            roleLabel.setFont(new Font(14));


            // Add elements to the grid
            userGrid.add(userIcon, 0, row);
            userGrid.add(nameLabel, 1, row);
            userGrid.add(emailLabel, 2, row);
            userGrid.add(phoneLabel, 3, row);
            userGrid.add(roleLabel, 4, row);

            row++;
        }
}
}