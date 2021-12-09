package com.intellij.tapestry.intellij.view;

import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.intellij.view.nodes.ComponentNode;
import com.intellij.tapestry.intellij.view.nodes.MixinNode;
import com.intellij.tapestry.intellij.view.nodes.PageNode;
import com.intellij.tapestry.lang.TmlFileType;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseEvent;

class ViewMouseListener extends MouseInputAdapter {

    private MouseEvent _firstMouseEvent = null;
    private final TapestryProjectViewPane _tapestryProjectViewPane;

    ViewMouseListener(TapestryProjectViewPane tapestryProjectViewPane) {
        _tapestryProjectViewPane = tapestryProjectViewPane;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent event) {
        if (_tapestryProjectViewPane.getTree().getSelectionPath() != null && _tapestryProjectViewPane.getTree().getSelectionPaths().length < 2) {
            Object selectedNode = ((DefaultMutableTreeNode) _tapestryProjectViewPane.getTree().getSelectionPath().getLastPathComponent()).getUserObject();
            Module module = (Module) _tapestryProjectViewPane.getData(PlatformCoreDataKeys.MODULE.getName());

            // If dragged node isn't a Page or Component or Mixin don't drag
            if (!(selectedNode instanceof PageNode) && !(selectedNode instanceof ComponentNode) && !(selectedNode instanceof MixinNode)) {
                return;
            }

            // If there's no file opened don't drag
            if (FileEditorManager.getInstance(_tapestryProjectViewPane.getProject()).getSelectedFiles().length == 0) {
                return;
            }

            PsiFile fileInEditor = PsiManager.getInstance(_tapestryProjectViewPane.getProject()).findFile(
                    FileDocumentManager.getInstance().getFile(FileEditorManager.getInstance(_tapestryProjectViewPane.getProject()).getSelectedTextEditor().getDocument())
            );
            FileType typeFileInEditor = fileInEditor.getFileType();

            // If the file in editor isn't either JAVA or TML don't drag
            if (!(fileInEditor instanceof PsiClassOwner) && !typeFileInEditor.equals(TmlFileType.INSTANCE)) {
                return;
            }

            // If the file in editor isn't writable or isn't part of the module where the drag is from don't drag
            Module moduleForFile = ProjectRootManager.getInstance(_tapestryProjectViewPane.getProject()).getFileIndex().getModuleForFile(fileInEditor.getVirtualFile());
            if (!fileInEditor.isWritable() || moduleForFile == null || !moduleForFile.equals(module)) {
                return;
            }

            // If the file in the editor is a TML file
            if (typeFileInEditor.equals(TmlFileType.INSTANCE)) {
                // If the file doesn't declare the Tapestry namespace don't drag
                if (TapestryUtils.getTapestryNamespacePrefix((XmlFile) fileInEditor) == null) {
                    return;
                }

                // Don't drag mixins to templates
                if (selectedNode instanceof MixinNode) {
                    return;
                }
            }

            // If the file in the editor is a JAVA file
            if (fileInEditor instanceof PsiClassOwner) {
                PsiClass psiClass = IdeaUtils.findPublicClass(fileInEditor);

                if (psiClass == null) {
                    return;
                }

                IntellijJavaClassType elementClass = new IntellijJavaClassType(module, psiClass.getContainingFile());
                PresentationLibraryElement presentationLibraryElement;
                try {
                    // Check if the drop target is a valid presentation element
                    presentationLibraryElement = PresentationLibraryElement.createProjectElementInstance(elementClass, TapestryModuleSupportLoader.getTapestryProject(module));
                } catch (NotTapestryElementException e) {
                    return;
                }

                // If dropping on a page class
                if (presentationLibraryElement.getElementType().equals(PresentationLibraryElement.ElementType.PAGE))
                // Can only drop Pages and Components
                {
                    if (!(selectedNode instanceof PageNode) && !(selectedNode instanceof ComponentNode)) {
                        return;
                    }
                }

                // If dropping on a component class
                if (presentationLibraryElement.getElementType().equals(PresentationLibraryElement.ElementType.COMPONENT))
                // Can only drop Pages and Components
                {
                    if (!(selectedNode instanceof PageNode) && !(selectedNode instanceof ComponentNode) && !(selectedNode instanceof MixinNode)) {
                        return;
                    }
                }

                // If dropping on a mixin class
                if (presentationLibraryElement.getElementType().equals(PresentationLibraryElement.ElementType.MIXIN))
                // Can only drop Pages and Components
                {
                    if (!(selectedNode instanceof PageNode)) {
                        return;
                    }
                }
            }

            _firstMouseEvent = event;
            event.consume();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(MouseEvent event) {
        if (_firstMouseEvent != null) {
            event.consume();

            int dx = Math.abs(event.getX() - _firstMouseEvent.getX());
            int dy = Math.abs(event.getY() - _firstMouseEvent.getY());
            //Arbitrarily define a 5-pixel shift as the
            //official beginning of a drag.
            if (dx > 5 || dy > 5) {
                //This is a drag, not a click.
                JComponent component = (JComponent) event.getSource();
                TransferHandler handler = component.getTransferHandler();
                //Tell the transfer handler to initiate the drag.
                handler.exportAsDrag(component, _firstMouseEvent, TransferHandler.COPY);
                _firstMouseEvent = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent event) {
        _firstMouseEvent = null;
    }
}