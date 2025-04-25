module dk.easv.blsgn.intgrpbelsign {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.blsgn.intgrpbelsign to javafx.fxml;
    exports dk.easv.blsgn.intgrpbelsign;
    exports dk.easv.blsgn.intgrpbelsign.gui;
    opens dk.easv.blsgn.intgrpbelsign.gui to javafx.fxml;
    exports dk.easv.blsgn.intgrpbelsign.gui.controllers;
    opens dk.easv.blsgn.intgrpbelsign.gui.controllers to javafx.fxml;
}