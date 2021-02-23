package org.sireum.docktabfx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class DockablePaneTests {

    // menu item ids for the the default tab context menu. see: {@link DockableSkin#createDefaultContextMenu(Tab)}
    public static final int CLOSE_TAB_MENU_ITEM = 0;
    public static final int CLOSE_OTHERS_MENU_ITEM = 1;
    public static final int CLOSE_GROUP_MENU_ITEM = 2;
    // public static final int SEPARATOR_MENU_ITEM = 3; // just a "separator" for design purposes
    public static final int SPLIT_VERTICALLY_MENU_ITEM = 4;
    public static final int SPLIT_HORIZONTALLY_MENU_ITEM = 5;

    private Stage stage;
    private Scene scene;
    private DockablePane pane;
    private Tab tab1;
    private Tab tab2;
    private Tab tab3;

    @Start
    private void start(@NotNull Stage stage) {
        this.stage = stage;
        this.pane = new DockablePane();
        this.tab1 = pane.addTab("tab1", new TextArea("tab 1"));
        this.tab2 = pane.addTab("tab2", new TextArea("tab 2"));
        this.tab3 = pane.addTab("tab3", new TextArea("tab 3"));
        this.scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    @Stop
    private void stop() {
        stage.close();
    }

    @Test
    void get_tab_pane_sanity_check_test() {
        final TabPane tabPane = tab1.getTabPane();
        assertSame(tabPane, tab2.getTabPane());
        assertSame(tabPane, tab3.getTabPane());
        assertTrue(tabPane.getTabs().contains(tab1));
        assertTrue(tabPane.getTabs().contains(tab2));
        assertTrue(tabPane.getTabs().contains(tab3));
    }

    @Test
    void close_tabs_test(FxRobot robot) {
        final TabPane tabPane = getInitialTabPane();

        clickTabMenuItem(robot, scene, tab1, CLOSE_TAB_MENU_ITEM);
        assertFalse(tabPane.getTabs().contains(tab1));
        assertTrue(tabPane.getTabs().contains(tab2));
        assertTrue(tabPane.getTabs().contains(tab3));

        clickTabMenuItem(robot, scene, tab2, CLOSE_TAB_MENU_ITEM);
        assertFalse(tabPane.getTabs().contains(tab1));
        assertFalse(tabPane.getTabs().contains(tab2));
        assertTrue(tabPane.getTabs().contains(tab3));

        clickTabMenuItem(robot, scene, tab3, CLOSE_TAB_MENU_ITEM);
        assertFalse(tabPane.getTabs().contains(tab1));
        assertFalse(tabPane.getTabs().contains(tab2));
        assertFalse(tabPane.getTabs().contains(tab3));
    }

    @Test
    void close_other_tabs_tab1_test(FxRobot robot) {
        final TabPane tabPane = getInitialTabPane();

        clickTabMenuItem(robot, scene, tab1, CLOSE_OTHERS_MENU_ITEM);
        assertTrue(tabPane.getTabs().contains(tab1));
        assertFalse(tabPane.getTabs().contains(tab2));
        assertFalse(tabPane.getTabs().contains(tab3));
    }

    @Test
    void close_other_tabs_tab2_test(FxRobot robot) {
        final TabPane tabPane = getInitialTabPane();

        clickTabMenuItem(robot, scene, tab2, CLOSE_OTHERS_MENU_ITEM);
        assertFalse(tabPane.getTabs().contains(tab1));
        assertTrue(tabPane.getTabs().contains(tab2));
        assertFalse(tabPane.getTabs().contains(tab3));
    }

    @Test
    void close_other_tabs_tab3_test(FxRobot robot) {
        final TabPane tabPane = getInitialTabPane();

        clickTabMenuItem(robot, scene, tab3, CLOSE_OTHERS_MENU_ITEM);
        assertFalse(tabPane.getTabs().contains(tab1));
        assertFalse(tabPane.getTabs().contains(tab2));
        assertTrue(tabPane.getTabs().contains(tab3));
    }

    @Test
    void split_vertical_tab1_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        // split tab 1
        clickTabMenuItem(robot, scene, tab1, SPLIT_VERTICALLY_MENU_ITEM);
        assertFalse(oldTabPane.getTabs().contains(tab1));
        assertTrue(oldTabPane.getTabs().contains(tab2));
        assertTrue(oldTabPane.getTabs().contains(tab3));

        final TabPane newTabPane = tab1.getTabPane();
        assertTrue(newTabPane.getTabs().contains(tab1));
        assertFalse(newTabPane.getTabs().contains(tab2));
        assertFalse(newTabPane.getTabs().contains(tab3));
    }

    @Test
    void split_vertical_tab2_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        // split tab 2
        clickTabMenuItem(robot, scene, tab2, SPLIT_VERTICALLY_MENU_ITEM);
        assertTrue(oldTabPane.getTabs().contains(tab1));
        assertFalse(oldTabPane.getTabs().contains(tab2));
        assertTrue(oldTabPane.getTabs().contains(tab3));

        final TabPane newTabPane = tab2.getTabPane();
        assertFalse(newTabPane.getTabs().contains(tab1));
        assertTrue(newTabPane.getTabs().contains(tab2));
        assertFalse(newTabPane.getTabs().contains(tab3));
    }

    @Test
    void split_vertical_tab3_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        // split tab 3
        clickTabMenuItem(robot, scene, tab3, SPLIT_VERTICALLY_MENU_ITEM);
        assertTrue(oldTabPane.getTabs().contains(tab1));
        assertTrue(oldTabPane.getTabs().contains(tab2));
        assertFalse(oldTabPane.getTabs().contains(tab3));

        final TabPane newTabPane = tab3.getTabPane();
        assertFalse(newTabPane.getTabs().contains(tab1));
        assertFalse(newTabPane.getTabs().contains(tab2));
        assertTrue(newTabPane.getTabs().contains(tab3));
    }

    @Test
    void split_horizontal_tab1_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        // split tab 1
        clickTabMenuItem(robot, scene, tab1, SPLIT_HORIZONTALLY_MENU_ITEM);
        assertFalse(oldTabPane.getTabs().contains(tab1));
        assertTrue(oldTabPane.getTabs().contains(tab2));
        assertTrue(oldTabPane.getTabs().contains(tab3));

        final TabPane newTabPane = tab1.getTabPane();
        assertTrue(newTabPane.getTabs().contains(tab1));
        assertFalse(newTabPane.getTabs().contains(tab2));
        assertFalse(newTabPane.getTabs().contains(tab3));
    }

    @Test
    void split_horizontal_tab2_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        // split tab 2
        clickTabMenuItem(robot, scene, tab2, SPLIT_HORIZONTALLY_MENU_ITEM);
        assertTrue(oldTabPane.getTabs().contains(tab1));
        assertFalse(oldTabPane.getTabs().contains(tab2));
        assertTrue(oldTabPane.getTabs().contains(tab3));

        final TabPane newTabPane = tab2.getTabPane();
        assertFalse(newTabPane.getTabs().contains(tab1));
        assertTrue(newTabPane.getTabs().contains(tab2));
        assertFalse(newTabPane.getTabs().contains(tab3));
    }

    @Test
    void split_horizontal_tab3_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        // split tab 3
        clickTabMenuItem(robot, scene, tab3, SPLIT_HORIZONTALLY_MENU_ITEM);
        assertTrue(oldTabPane.getTabs().contains(tab1));
        assertTrue(oldTabPane.getTabs().contains(tab2));
        assertFalse(oldTabPane.getTabs().contains(tab3));

        final TabPane newTabPane = tab3.getTabPane();
        assertFalse(newTabPane.getTabs().contains(tab1));
        assertFalse(newTabPane.getTabs().contains(tab2));
        assertTrue(newTabPane.getTabs().contains(tab3));
    }

    @Test
    void double_split_vertical_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        split_vertical_tab1_test(robot);

        // split tab 1
        clickTabMenuItem(robot, scene, tab2, SPLIT_VERTICALLY_MENU_ITEM);
        assertFalse(oldTabPane.getTabs().contains(tab1));
        assertFalse(oldTabPane.getTabs().contains(tab2));
        assertTrue(oldTabPane.getTabs().contains(tab3));

        final TabPane secondNewTabPane = tab2.getTabPane();
        assertFalse(secondNewTabPane.getTabs().contains(tab1));
        assertTrue(secondNewTabPane.getTabs().contains(tab2));
        assertFalse(secondNewTabPane.getTabs().contains(tab3));

        assertNotSame(tab1.getTabPane(), tab2.getTabPane());
        assertNotSame(tab2.getTabPane(), tab3.getTabPane());
        assertNotSame(tab1.getTabPane(), tab3.getTabPane());
    }

    @Test
    void double_split_horizontal_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        split_horizontal_tab1_test(robot);

        // split tab 1
        clickTabMenuItem(robot, scene, tab2, SPLIT_HORIZONTALLY_MENU_ITEM);
        assertFalse(oldTabPane.getTabs().contains(tab1));
        assertFalse(oldTabPane.getTabs().contains(tab2));
        assertTrue(oldTabPane.getTabs().contains(tab3));

        final TabPane secondNewTabPane = tab2.getTabPane();
        assertFalse(secondNewTabPane.getTabs().contains(tab1));
        assertTrue(secondNewTabPane.getTabs().contains(tab2));
        assertFalse(secondNewTabPane.getTabs().contains(tab3));

        assertNotSame(tab1.getTabPane(), tab2.getTabPane());
        assertNotSame(tab2.getTabPane(), tab3.getTabPane());
        assertNotSame(tab1.getTabPane(), tab3.getTabPane());
    }

    @Test
    void double_split_vertical_horizontal_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        split_vertical_tab1_test(robot);

        // split tab 1
        clickTabMenuItem(robot, scene, tab2, SPLIT_HORIZONTALLY_MENU_ITEM);
        assertFalse(oldTabPane.getTabs().contains(tab1));
        assertFalse(oldTabPane.getTabs().contains(tab2));
        assertTrue(oldTabPane.getTabs().contains(tab3));

        final TabPane secondNewTabPane = tab2.getTabPane();
        assertFalse(secondNewTabPane.getTabs().contains(tab1));
        assertTrue(secondNewTabPane.getTabs().contains(tab2));
        assertFalse(secondNewTabPane.getTabs().contains(tab3));

        assertNotSame(tab1.getTabPane(), tab2.getTabPane());
        assertNotSame(tab2.getTabPane(), tab3.getTabPane());
        assertNotSame(tab1.getTabPane(), tab3.getTabPane());
    }

    @Test
    void double_split_horizontal_vertical_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        split_horizontal_tab1_test(robot);

        // split tab 1
        clickTabMenuItem(robot, scene, tab2, SPLIT_VERTICALLY_MENU_ITEM);
        assertFalse(oldTabPane.getTabs().contains(tab1));
        assertFalse(oldTabPane.getTabs().contains(tab2));
        assertTrue(oldTabPane.getTabs().contains(tab3));

        final TabPane secondNewTabPane = tab2.getTabPane();
        assertFalse(secondNewTabPane.getTabs().contains(tab1));
        assertTrue(secondNewTabPane.getTabs().contains(tab2));
        assertFalse(secondNewTabPane.getTabs().contains(tab3));

        assertNotSame(tab1.getTabPane(), tab2.getTabPane());
        assertNotSame(tab2.getTabPane(), tab3.getTabPane());
        assertNotSame(tab1.getTabPane(), tab3.getTabPane());
    }

    @Test
    void close_group_tabs_test(FxRobot robot) {
        // split into groups: (tab2,tab3) (tab1)
        final TabPane oldTabPane = getInitialTabPane();
        split_horizontal_tab1_test(robot);
        final TabPane newTabPane = tab1.getTabPane();

        // close tab2's tab group
        clickTabMenuItem(robot, scene, tab2, CLOSE_GROUP_MENU_ITEM);
        assertTrue(oldTabPane.getTabs().isEmpty());
        assertTrue(newTabPane.getTabs().contains(tab1));
        assertNull(tab2.getTabPane());
        assertNull(tab3.getTabPane());
    }

    @Test
    void split_vertical_tab1_drag_test(FxRobot robot) {
        final TabPane oldTabPane = getInitialTabPane();

        split_vertical_tab1_test(robot);

        assertNotSame(tab1.getTabPane(), tab2.getTabPane());
        assertSame(tab2.getTabPane(), tab3.getTabPane());
        dragTab(robot, tab2, tab1); // drag tab2 to the TabPane of tab1
        assertSame(tab1.getTabPane(), tab2.getTabPane());
        assertNotSame(tab2.getTabPane(), tab3.getTabPane());
    }

    private static void clickTabMenuItem(FxRobot robot, Scene scene, Tab tab, int menuItem) {
        final Bounds bounds = robot.bounds(tab.getGraphic()).query();
        robot.moveTo(tab.getGraphic());
        robot.interact(() -> {
            tab.getContextMenu().show(scene.getWindow(), bounds.getCenterX(), bounds.getCenterY());
            final EventHandler<ActionEvent> action = tab.getContextMenu().getItems().get(menuItem).getOnAction();
            if (action != null) {
                action.handle(new ActionEvent());
            }
            tab.getContextMenu().hide();
        });
    }

    // todo can testfx handle dragging naturally? .drag() .dragTo() .press() .release() methods dont seem to work
    private void dragTab(FxRobot robot, Tab from, Tab to) {
        robot.moveTo(from.getGraphic());
        robot.moveTo(to.getGraphic());
        // cheat by simulating a drag tab (since it doesn't seem to work with testfx)
        robot.interact(() -> {
            pane.removeTab(from);
            robot.interact(() -> to.getTabPane().getTabs().add(from));
        });
    }

    @NotNull
    private TabPane getInitialTabPane() {
        get_tab_pane_sanity_check_test();
        return tab1.getTabPane();
    }

}
