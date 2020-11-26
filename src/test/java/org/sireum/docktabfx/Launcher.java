package org.sireum.docktabfx;

import javafx.application.Application;

/**
 * Starts the {@link ManyTabsSandbox} javafx testbed application.
 * <br>
 * This runner is separate from the {@link ManyTabsSandbox} due to unintuitive behavior of the JPMS (jigsaw).
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(ManyTabsSandbox.class);
    }
}
