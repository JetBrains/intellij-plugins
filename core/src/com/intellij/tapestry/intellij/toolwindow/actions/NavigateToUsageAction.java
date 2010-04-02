package com.intellij.tapestry.intellij.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiField;
import com.intellij.tapestry.core.model.presentation.InjectedElement;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import com.intellij.tapestry.intellij.toolwindow.nodes.EmbeddedComponentNode;
import com.intellij.tapestry.intellij.toolwindow.nodes.InjectedPageNode;
import com.intellij.tapestry.intellij.util.Icons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class NavigateToUsageAction extends AnAction {

    private final JTree _tree;

    public NavigateToUsageAction(JTree tree) {
        super("Navigate to Usage", "Navigate to part of code where the selected element is used", Icons.REFERENCE);

        _tree = tree;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(AnActionEvent event) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) _tree.getSelectionPath().getLastPathComponent();
        Object selectedObject = selectedNode.getUserObject();

        if (selectedObject instanceof PresentationLibraryElement || selectedObject instanceof InjectedElement) {
            PsiField field = null;

            // Embedded component
            if (selectedNode instanceof EmbeddedComponentNode) {
                field = ((IntellijJavaField) ((EmbeddedComponentNode) selectedNode).getInjectedComponent().getField()).getPsiField();
            }

            // Injected page
            if (selectedNode instanceof InjectedPageNode) {
                field = ((IntellijJavaField) ((InjectedPageNode) selectedNode).getInjectedPage().getField()).getPsiField();
            }

            if (field != null) {
                field.navigate(true);
            }
        }
    }
}
