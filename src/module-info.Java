module puzzlesolver {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.swing;
    requires java.desktop;
    
    exports main;
    exports puzzle;
    exports file;
    opens main;
}