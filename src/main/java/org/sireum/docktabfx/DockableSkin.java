/*
 * Copyright (c) 2020, Matthew Weis, Kansas State University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sireum.docktabfx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.util.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

// todo: probably make this public, even if users should not use it? if public, then ".getSkin()" can be casted
//       to DockableSkin in case users need to handle specific case. But this is meaningless if fields aren't made
//       public properties
public class DockableSkin implements Skin<DockablePane>, Dockable {

    // todo make non final property with null initializer. make @nullable?
    // todo idea: should this (and all components) be made available
    private final SimpleObjectProperty<Tab> targetTab = new SimpleObjectProperty<>();

    // todo make public property, add factory?
    @Nullable
    private SplitPane rootSplitPane = new SplitPane();

    @Nullable
    private TabPane rootTabPane;

    private void initRootTabPaneIfEmpty() {
        if (rootSplitPane != null && rootTabPane == null) {
            rootTabPane = createTabPane();
            rootSplitPane.getItems().add(rootTabPane);
        }
    }

    @Nullable
    @Override
    public DockablePane getSkinnable() {
        // getNode() returns null iff dispose() has been called
        final Styleable styleableParent = Optional.ofNullable(getNode())
                .map(Node::getStyleableParent)
                .orElse(null);
        return (DockablePane) styleableParent;
    }

    @Override
    public Node getNode() {
        return rootSplitPane; // returns null iff dispose() has been called
    }

    @Override
    public void dispose() {
        // after calling dispose(), getNode() and getSkinnable() should return null (as per javafx rules)
        rootTabPane = null;
        rootSplitPane = null;
    }

    @NotNull
    @Override
    public Tab addTab(@NotNull String name, @NotNull Node content) {
        initRootTabPaneIfEmpty();

        final Label label = new Label(name); // todo tabLabelFactory here? or just let them edit
        final Tab tab = new Tab(); // todo use factory here? or just let them edit afterwards

        label.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> targetTab.set(tab));

        tab.setGraphic(label);
        tab.setContent(content);

        final ContextMenu contextMenu = mapOrFallback(getSkinnable(),
                DockablePane::getContextMenuFactory,
                it -> it.call(tab),
                () -> createDefaultContextMenu(tab));

        tab.setContextMenu(contextMenu);
        if (rootTabPane != null) { // check if disposed
            rootTabPane.getTabs().add(tab); // tabs are always added to the first tabPane
        }
        return tab;
    }

    // whenever an existing tab spawns with a new pane, the reference to that pane exists until the tab is closed
    // also the last closed tab is tracked by JavaFx
    // so we may need to recreate th tab when we move it?
    // but javafx might drop this for us later anyways:
    // https://stackoverflow.com/questions/31928294/closing-javafx-tabs-doesnt-release-memory-from-arraylists-and-tableviews-in-tha

    @Override
    public void removeTab(@NotNull Tab tab) {
        final TabPane tabPane = tab.getTabPane();
        if (tabPane != null) {
            tabPane.getTabs().remove(tab);
            closeTabPaneIfEmpty(tabPane);
        }
    }

    @Override
    public void removeAllOtherTabsInGroup(@NotNull Tab tab) {
        final TabPane tabPane = tab.getTabPane();
        if (tabPane != null) {
            final ObservableList<Tab> tabs = tabPane.getTabs();
            // using while loop because collection removal is indirectly called by removeTab
            while (tabs.size() > 1) {
                final Tab removalCandidate = tabs.get(0);
                if (removalCandidate != tab) {
                    tabs.remove(0);
                } else {
                    tabs.remove(1); // this works assuming there is never a change allowing tab duplicates
                }
            }
        }
    }

    @Override
    public void removeGroup(@NotNull Tab tab) {
        final TabPane tabPane = tab.getTabPane();
        if (tabPane != null) {
            tabPane.getTabs().clear();
            closeTabPaneIfEmpty(tabPane);
        }
    }

    @Override
    public void removeAllOtherGroups(@NotNull Tab tab) {
        final TabPane tabPane = tab.getTabPane();
        if (tabPane != null) {
            // remove this tab pane from its parent
            final SplitPane parent = findMostDirectContainer(tabPane);
            if (parent != null) {
                parent.getItems().remove(tabPane);
            }
            // remove all other nodes from the hierarchy
            if (rootSplitPane != null) { // if not disposed
                rootSplitPane.getItems().clear();
                rootSplitPane.getItems().add(tabPane); //https://stackoverflow.com/questions/31928294/closing-javafx-tabs-doesnt-release-memory-from-arraylists-and-tableviews-in-thab
            }
        }
    }

    @Override
    public void removeAll() {
        if (rootSplitPane != null) { // if not disposed
            rootSplitPane.getItems().clear();
        }
    }

    // removes empty TabPanes contained in SplitPanes... but what if users hold an outside ref?
    private void clean(@NotNull SplitPane container) {
        // container's children are SplitPane or TabPane
        // children that are SplitPane also guarantee this contract
        for (Node node : container.getItems()) {
            if (node instanceof TabPane) {
                closeTabPaneIfEmpty((TabPane) node);
            } else if (node instanceof SplitPane) {
                clean((SplitPane) node);
            }
        }
    }

    @Nullable
    private static TabPane findUppermostTabPane(SplitPane container) {
        // first check all items
        for (Node node : container.getItems()) {
            if (node instanceof TabPane) {
                return (TabPane) node;
            }
        }

        // then go deeper down tree if needed
        for (Node node : container.getItems()) {
            if (node instanceof SplitPane) {
                // check recursively if tab holds SplitPane
                final SplitPane splitPane = (SplitPane) node;
                final TabPane tabPane = findUppermostTabPane(splitPane);

                if (tabPane != null) {
                    return tabPane;
                }
            }
        }

        return null;
    }

    @Nullable
    private SplitPane findMostDirectContainer(Node child) {
        if (rootSplitPane != null) { // check if disposed
            return findMostDirectContainer(rootSplitPane, child);
        }
        return null;
    }

    // inner recursive impl
    @Nullable
    private static SplitPane findMostDirectContainer(SplitPane splitPane, Node child) {
        for (Node node : splitPane.getItems()) {
            if (node == child) { // implicitly means node must also be instance of TabPane
                return splitPane;
            }
        }

        for (Node node : splitPane.getItems()) {
            if (node instanceof SplitPane) {
                final SplitPane result = findMostDirectContainer((SplitPane) node, child);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    @Override
    public void splitTab(@NotNull Tab tab, @NotNull Orientation orientation) {
        final TabPane tabPane = tab.getTabPane();
        // if non-null and there are at least 2 tabs (otherwise there's nothing to split)
        if (tabPane != null && tabPane.getTabs().size() > 1) {
            final SplitPane parent = findMostDirectContainer(tabPane);
            if (parent != null) {
                clean(parent);
                // if 1 item, just add tabPane directly
                final TabPane newTabPane;
                if (parent.getItems().size() < 2) {
                    parent.setOrientation(oppositeOrientation(orientation));
                    newTabPane = createTabPane();
                    tabPane.getTabs().remove(tab);
                    newTabPane.getTabs().add(tab);
                    parent.getItems().add(newTabPane);
                } else { // if more than one, put tabPane into a splitPane
                    tabPane.getTabs().remove(tab);
                    newTabPane = createTabPane();
                    newTabPane.getTabs().add(tab);
                    final int placement = parent.getItems().indexOf(tabPane);
                    parent.getItems().remove(placement);
                    final SplitPane newSplitPane = createSplitPane(tabPane, newTabPane);
                    newSplitPane.setOrientation(oppositeOrientation(orientation));
                    parent.getItems().add(placement, newSplitPane);
                }
            }
        }
    }

    private static Orientation oppositeOrientation(Orientation orientation) {
        if (orientation == Orientation.HORIZONTAL) {
            return Orientation.VERTICAL;
        } else if (orientation == Orientation.VERTICAL) {
            return Orientation.HORIZONTAL;
        } else {
            // impossible unless a new value is added down the road or something
            throw new IllegalStateException("Orientation must equal HORIZONTAL or VERTICAL.");
        }
    }

    private SplitPane createSplitPane(Node... items) {
        // todo allow for factory like with createTabPane
        final SplitPane splitPane = new SplitPane(items);
        splitPane.getItems().addListener((ListChangeListener<? super Node>) c -> {
            if (splitPane.getItems().isEmpty()) {
                final SplitPane parent = findMostDirectContainer(splitPane);
                if (parent != null) {
                    parent.getItems().remove(splitPane);
                }
            }
        });

        return splitPane;
    }

    private TabPane createTabPane() {
        final TabPane tabPane = mapOrFallback(getSkinnable(), DockablePane::getTabPaneFactory,
                Builder::build, this::createDefaultTabPane);

        tabPane.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (targetTab.get() != null) {
                final PickResult pickResult = event.getPickResult();

                Node node = pickResult.getIntersectedNode();
                while (node != null && !(node instanceof TabPane)) {
                    node = node.getParent();
                }

                final TabPane targetTabPane = (TabPane) node;
                final Tab tab = targetTab.get();

                if (targetTabPane != null && tab != null && tabPane != targetTabPane) {
                    // consuming the event prevents index error handling upstream
                    event.consume();
                    tabPane.getTabs().remove(tab);
                    targetTabPane.getTabs().add(tab);
                    System.out.println("HERE!");
                    closeTabPaneIfEmpty(tabPane);
                }
                targetTab.set(null);
            }
        });

        // automatically close tabPanes if 0 items
        tabPane.getTabs().addListener((ListChangeListener<? super Tab>) c -> closeTabPaneIfEmpty(tabPane));
        return tabPane;
    }

    @NotNull
    private static <T,R> R mapOrFallback(@Nullable T initial, Function<T, @Nullable R> map, @NotNull Supplier<R> fallback) {
        if (initial == null) {
            return fallback.get();
        } else {
            return Objects.requireNonNullElseGet(map.apply(initial), fallback);
        }
    }

    @NotNull
    private static <T,U,R> R mapOrFallback(@Nullable T initial, Function<T, @Nullable U> map1, Function<U, @Nullable R> map2, @NotNull Supplier<R> fallback) {
        if (initial == null) {
            return fallback.get();
        } else {
            return mapOrFallback(map1.apply(initial), map2, fallback);
        }
    }

    private void closeTabPaneIfEmpty(TabPane tabPane) {
        if (tabPane.getTabs().isEmpty()) {
            final SplitPane parent = findMostDirectContainer(tabPane);
            if (parent != null) {
                parent.getItems().remove(tabPane);

                // if the closing tabPane was the root then set rootTabPane to another tabPane
                // (or null if no other tabPane exists)
                if (rootSplitPane != null && tabPane == rootTabPane) {
                    rootTabPane = findUppermostTabPane(rootSplitPane);
                }
            }
        }
    }

    // thread-safe lazy loader of "split vertical" menuitem image
    private static final class VerticalSplitImageLazyLoader {
        private static final Image INSTANCE =
                new Image(DockableSkin.class.getResource("vertical-split.png").toExternalForm());
    }

    // thread-safe lazy loader of "split horizontal" menuitem image
    private static final class HorizontalSplitImageLazyLoader {
        private static final Image INSTANCE =
                new Image(DockableSkin.class.getResource("horizontal-split.png").toExternalForm());
    }

    private TabPane createDefaultTabPane() {
        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        return tabPane;
    }

    private ContextMenu createDefaultContextMenu(Tab tab) {
        final MenuItem closeMenuItem = new MenuItem("Close");
        closeMenuItem.setOnAction(event -> removeTab(tab));

        final MenuItem closeOthersMenuItem = new MenuItem("Close Others");
        closeOthersMenuItem.setOnAction(event -> removeAllOtherTabsInGroup(tab));

        final MenuItem closeGroupMenuItem = new MenuItem("Close Group");
        closeGroupMenuItem.setOnAction(event -> removeGroup(tab));

        final ImageView v = new ImageView(VerticalSplitImageLazyLoader.INSTANCE);
        final ImageView h = new ImageView(HorizontalSplitImageLazyLoader.INSTANCE);

        final MenuItem splitVerticallyMenuItem = new MenuItem("Split Vertically", v);
        splitVerticallyMenuItem.setOnAction(event -> splitTab(tab, Orientation.VERTICAL));

        final MenuItem splitHorizontallyMenuItem = new MenuItem("Split Horizontally", h);
        splitHorizontallyMenuItem.setOnAction(event -> splitTab(tab, Orientation.HORIZONTAL));

        return new ContextMenu(closeMenuItem, closeOthersMenuItem, closeGroupMenuItem,
                new SeparatorMenuItem(), splitVerticallyMenuItem, splitHorizontallyMenuItem);
    }

}
