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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.util.Builder;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DockablePane extends Control implements Dockable {

    private ObjectProperty<Builder<TabPane>> tabPaneFactory = null;
    private ObjectProperty<Callback<Tab, ContextMenu>> contextMenuFactory = null;

    public final ObjectProperty<Builder<TabPane>> tabPaneFactoryProperty() {
        if (tabPaneFactory == null) {
            tabPaneFactory = new SimpleObjectProperty<>(this, "tabPaneFactory");
        }
        return tabPaneFactory;
    }

    public final void setTabPaneFactory(Builder<TabPane> value) {
        tabPaneFactoryProperty().set(value);
    }

    @Nullable
    public final Builder<TabPane> getTabPaneFactory() {
        return tabPaneFactory == null ? null : tabPaneFactory.get();
    }

    public final ObjectProperty<Callback<Tab, ContextMenu>> contextMenuFactoryProperty() {
        if (contextMenuFactory == null) {
            contextMenuFactory = new SimpleObjectProperty<>(this, "contextMenuFactory");
        }
        return contextMenuFactory;
    }

    public final void setContextMenuFactory(Callback<Tab, ContextMenu> value) {
        contextMenuFactoryProperty().set(value);
    }

    @Nullable
    public final Callback<Tab, ContextMenu> getContextMenuFactory() {
        return contextMenuFactory == null ? null : contextMenuFactory.get();
    }

    public DockablePane() {
        setSkin(new DockableSkin());
    }

    @NotNull
    @Override
    public Tab addTab(@NotNull String name, @NotNull Node content) {
        final DockableSkin skin = (DockableSkin) getSkin();
        return skin.addTab(name, content);
    }

    @Override
    public void removeTab(@NotNull Tab tab) {
        final DockableSkin skin = (DockableSkin) getSkin();
        skin.removeTab(tab);
    }

    @Override
    public void removeOthers(@NotNull Tab tab) {
        final DockableSkin skin = (DockableSkin) getSkin();
        skin.removeOthers(tab);
    }

    @Override
    public void removeGroup(@NotNull Tab tab) {
        final DockableSkin skin = (DockableSkin) getSkin();
        skin.removeGroup(tab);
    }

    @Override
    public void splitTab(@NotNull Tab tab, @NotNull Orientation orientation) {
        final DockableSkin skin = (DockableSkin) getSkin();
        skin.splitTab(tab, orientation);
    }

}
