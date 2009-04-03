package com.intellij.tapestry.intellij.view.actions;

import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.tapestry.intellij.util.Icons;

public abstract class GroupElementFilesToggleAction extends ToggleAction {

    public GroupElementFilesToggleAction() {
        super("Group Element Files", "Group Element Files Like it's Class and Template in a Parent Node", Icons.GROUP_ELEMENT_FILES);
    }
}
