module xuexianchen.crazy8 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

                                requires com.almasb.fxgl.all;
    
    opens xuexianchen.crazy8 to javafx.fxml;
    exports xuexianchen.crazy8;
}