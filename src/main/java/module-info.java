module dk.easv.blsgn.intgrpbelsign {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.blsgn.intgrpbelsign to javafx.fxml;
    exports dk.easv.blsgn.intgrpbelsign;
}