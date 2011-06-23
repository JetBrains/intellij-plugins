package com.intellij.tapestry.intellij.view.actions;

import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.tapestry.core.util.TapestryIcons;

public abstract class ShowLibrariesTogleAction extends ToggleAction {

    public ShowLibrariesTogleAction() {
        super("Show Libraries", "Show/Hide Tapestry Libraries", TapestryIcons.SHOW_LIBRARIES);
    }
}
