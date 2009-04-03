package com.intellij.tapestry.intellij.view.actions;

import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.tapestry.intellij.util.Icons;

public abstract class ShowLibrariesTogleAction extends ToggleAction {

    public ShowLibrariesTogleAction() {
        super("Show Libraries", "Show/Hide Tapestry Libraries", Icons.SHOW_LIBRARIES);
    }
}
