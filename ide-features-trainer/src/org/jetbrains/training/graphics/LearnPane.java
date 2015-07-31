package org.jetbrains.training.graphics;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.components.impl.ComponentManagerImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * Created by karashevich on 27/07/15.
 */
public class LearnPane  {


    public JComponent getIdeEditorArea(Project project){
        FileEditorManager.getInstance(project);
        return null;
    }

}
