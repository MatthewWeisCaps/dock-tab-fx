package org.sireum.docktabfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ManyTabsSandbox extends Application {

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();

        final DockablePane pane = new DockablePane();
        root.getChildren().add(pane);

        pane.addTab("tab01", new TextArea("tab 01"));
        pane.addTab("tab02", new TextArea("tab 02"));
        pane.addTab("tab03", new TextArea("tab 03"));
        pane.addTab("tab04", new TextArea("tab 04"));
        pane.addTab("tab05", new TextArea("tab 05"));
        pane.addTab("tab06", new TextArea("tab 06"));
        pane.addTab("tab07", new TextArea("tab 07"));

        Scene s = new Scene(root, 720, 480);
        stage.setScene(s);
        stage.show();
    }

}
