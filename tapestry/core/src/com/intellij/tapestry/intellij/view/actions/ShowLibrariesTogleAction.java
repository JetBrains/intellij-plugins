package com.intellij.tapestry.intellij.view.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ToggleAction;

public abstract class ShowLibrariesTogleAction extends ToggleAction {

    public ShowLibrariesTogleAction() {
        super("Show Libraries", "Show/Hide Tapestry Libraries", AllIcons.Nodes.PpLibFolder);
    }
}
