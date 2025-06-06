module com.example.treasurehunt {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.treasurehunt to javafx.fxml;
    exports com.example.treasurehunt;
}