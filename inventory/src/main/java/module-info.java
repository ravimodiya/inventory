module com.inventory {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;

    opens com.inventory to javafx.fxml;
    exports com.inventory;
}
