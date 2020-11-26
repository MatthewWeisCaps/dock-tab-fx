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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.StackPane;
import javafx.util.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

// todo: probably make this public, even if users should not use it? if public, then ".getSkin()" can be casted
//       to DockableSkin in case users need to handle specific case. But this is meaningless if fields aren't made
//       public properties
class DockableSkin implements Skin<DockablePane>, Dockable {

    private StackPane rootNode = new StackPane(); // only changes once -- to become null on dispose

    // todo make non final property with null initializer
    // todo idea: should this (and all components) be made available
    private final SimpleObjectProperty<Tab> targetTab = new SimpleObjectProperty<>();

    // todo make public property, add factory
    private final SplitPane rootSplitPane = new SplitPane();

    // todo idea: make public property
    private TabPane rootTabPane;

    DockableSkin() {
        rootNode.getChildren().addAll(rootSplitPane);
    }

    private void initRootTabPaneIfEmpty() {
        if (rootTabPane == null) {
            rootTabPane = createTabPane();
            rootSplitPane.getItems().add(rootTabPane);
        }
    }

    @Override
    public DockablePane getSkinnable() {
        return (DockablePane) getNode().getStyleableParent();
    }

    @Override
    public Node getNode() {
        return rootNode;
    }

    @Override
    public void dispose() {
        rootNode = null; // see getNode()'s superclass javadoc for why dispose makes rootNode null
    }

    @NotNull
    @Override
    public Tab addTab(@NotNull String name, @NotNull Node content) {
        initRootTabPaneIfEmpty();

        final Label label = new Label(name);
        final Tab tab = new Tab();

        label.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> targetTab.set(tab));

        tab.setGraphic(label);
        tab.setContent(content);

        final ContextMenu contextMenu = mapOrFallback(getSkinnable().getContextMenuFactory(), it -> it.call(tab),
                () -> createDefaultContextMenu(tab));

        tab.setContextMenu(contextMenu);
        rootTabPane.getTabs().add(tab); // tabs are always added to the first tabPane
        return tab;
    }

    @Override
    public void removeTab(@NotNull Tab tab) {
        removeRecursive(tab, rootSplitPane);
    }

    @Override
    public void removeOthers(@NotNull Tab tab) {
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
            final ObservableList<Tab> tabs = tabPane.getTabs();
            // using while loop because collection removal is indirectly called by removeTab
            while (!tabs.isEmpty()) {
                removeTab(tabs.get(0));
            }
        }
    }

    private static boolean removeRecursive(@NotNull Tab tab, SplitPane container) {
        // container's children are SplitPane or TabPane
        // children that are SplitPane also guarantee this contract
        for (Node node : container.getItems()) {
            if (node instanceof TabPane) {
                final ObservableList<Tab> tabs = ((TabPane) node).getTabs();

                // check if tab exists at this level
                for (Tab potentialTab : tabs) {
                    if (tab == potentialTab) {
                        tabs.remove(tab);
                        return true;
                    }
                }
            } else if (node instanceof SplitPane) {
                // check recursively if tab holds SplitPane
                final SplitPane splitPane = (SplitPane) node;
                final boolean success = removeRecursive(tab, splitPane);

                if (success) {
                    return true; // return true iff successful, otherwise loop the rest at this depth
                }
            }
        }

        return false;
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

    private SplitPane findMostDirectContainer(Node child) {
        return findMostDirectContainer(rootSplitPane, child);
    }

    // inner recursive impl
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
        final TabPane tabPane = mapOrFallback(getSkinnable().getTabPaneFactory(), Builder::build, this::createDefaultTabPane);

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
    private static <T,R> R mapOrFallback(@Nullable T initial, Function<T, @Nullable R> map, Supplier<R> fallback) {
        if (initial == null) {
            return fallback.get();
        } else {
            return Objects.requireNonNullElseGet(map.apply(initial), fallback);
        }
    }

    private void closeTabPaneIfEmpty(TabPane tabPane) {
        if (tabPane.getTabs().isEmpty()) {
            final SplitPane parent = findMostDirectContainer(tabPane);
            if (parent != null) {
                parent.getItems().remove(tabPane);

                // if the closing tabPane was the root then set rootTabPane to another tabPane
                // (or null if no other tabPane exists)
                if (tabPane == rootTabPane) {
                    rootTabPane = findUppermostTabPane(rootSplitPane);
                }
            }
        }
    }

    // thread-safe lazy loader of vertical image
    private static final class VerticalSplitImageLazyLoader {
        static final Image INSTANCE = new Image(DockableSkin.class.getResource("vertical-split.png").toExternalForm());
    }

    // thread-safe lazy loader of horizontal image
    private static final class HorizontalSplitImageLazyLoader {
        static final Image INSTANCE = new Image(DockableSkin.class.getResource("horizontal-split.png").toExternalForm());
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
        closeOthersMenuItem.setOnAction(event -> removeOthers(tab));

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
