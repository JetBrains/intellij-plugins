package com.intellij.tapestry.intellij.view.actions;

import com.intellij.openapi.actionSystem.ToggleAction;
import icons.TapestryIcons;

public abstract class GroupElementFilesToggleAction extends ToggleAction {

    public GroupElementFilesToggleAction() {
        super("Group Element Files", "Group Element Files Like it's Class and Template in a Parent Node",
              TapestryIcons.GroupElementFiles);
    }
}
