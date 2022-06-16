package com.intellij.tapestry.intellij.actions.safedelete;

import com.intellij.ide.DeleteProvider;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.SafeDeleteRefactoring;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.view.TapestryProjectViewPane;
import com.intellij.tapestry.intellij.view.nodes.LibrariesNode;
import com.intellij.tapestry.intellij.view.nodes.PackageNode;
import com.intellij.tapestry.intellij.view.nodes.TapestryNode;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * Safe Delete action provider.
 */
public class SafeDeleteProvider implements DeleteProvider {
  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.EDT;
  }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteElement(@NotNull DataContext dataContext) {
        SafeDeleteRefactoring safeDeleteRefactoring;
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        int numberChildren;

        List<PsiElement> totalElementsToDelete = new ArrayList<>();
        for (TreePath treePath : TapestryProjectViewPane.getInstance(project).getSelectionPaths()) {
            PsiElement[] elements;
            List<PsiElement> elementsList = new ArrayList<>();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();

            // The selected node is a file
            if (((TapestryNode) node.getUserObject()).getElement() instanceof PsiFile) {
              elements = new PsiElement[]{(PsiFile)((TapestryNode)node.getUserObject()).getElement()};
              ContainerUtil.addAll(totalElementsToDelete, elements);
            }

            // The selected node is a presentation element
            if ((((TapestryNode) node.getUserObject()).getElement() instanceof PresentationLibraryElement)) {
                PsiElement elementClass = ((IntellijResource) ((PresentationLibraryElement) ((TapestryNode) node.getUserObject()).getElement()).getElementClass().getFile()).getPsiFile();
                totalElementsToDelete.add(elementClass);

                for (IResource template : ((PresentationLibraryElement) ((TapestryNode) node.getUserObject()).getElement()).getTemplate())
                    totalElementsToDelete.add(((IntellijResource) template).getPsiFile());

                for (IResource catalog : ((PresentationLibraryElement) ((TapestryNode) node.getUserObject()).getElement()).getMessageCatalog())
                    totalElementsToDelete.add(((IntellijResource) catalog).getPsiFile());
            }

            // The selected node is a package
            if (node.getUserObject() instanceof PackageNode) {
                boolean expanded = TapestryProjectViewPane.getInstance(project).getTree().isExpanded(new TreePath(node.getPath()));
                DefaultMutableTreeNode starterNode = node;

                totalElementsToDelete.add((PsiElement) ((TapestryNode) node.getUserObject()).getElement());

                // Exist nodes
                while (node != null && (node.getUserObject() instanceof PackageNode || ((TapestryNode) node.getUserObject()).getElement() instanceof PresentationLibraryElement)) {
                    numberChildren = ((TapestryNode) node.getUserObject()).getChildren().length;

                    TapestryProjectViewPane.getInstance(project).getTree().expandPath(new TreePath(node.getPath()));

                    // Search all the children
                    for (int i = 0; i < numberChildren; i++) {
                        DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                        // The node is a presentation element
                        if (((TapestryNode) child.getUserObject()).getElement() instanceof PresentationLibraryElement) {
                            elementsList = addElementToDelete(child, elementsList);
                        }
                    }
                    if (numberChildren > 0) {
                        node = node.getNextNode();
                    } else {
                        node = null;
                    }
                }
                totalElementsToDelete.addAll(elementsList);

                if (!expanded) {
                    TapestryProjectViewPane.getInstance(project).getTree().collapsePath(new TreePath(starterNode.getPath()));
                }
            }
        }
      safeDeleteRefactoring = RefactoringFactory.getInstance(project).createSafeDelete(
        PsiUtilCore.toPsiElementArray(totalElementsToDelete));
        safeDeleteRefactoring.setPreviewUsages(true);
        safeDeleteRefactoring.run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canDeleteElement(@NotNull DataContext dataContext) {
        Project project = dataContext.getData(CommonDataKeys.PROJECT);

        if (project == null || TapestryProjectViewPane.getInstance(project).getSelectionPaths() == null) {
            return false;
        }

        for (TreePath treePath : TapestryProjectViewPane.getInstance(project).getSelectionPaths()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            boolean canDelete = false;

            // The element to delete is a presentation element.
            if ((((TapestryNode) node.getUserObject()).getElement() instanceof PresentationLibraryElement) &&
                    ((PresentationLibraryElement) ((TapestryNode) node.getUserObject()).getElement()).getLibrary().getId().equals(TapestryProject.APPLICATION_LIBRARY_ID)) {
                canDelete = true;
            }

            // The element to delete is an TML file
            if (((TapestryNode) node.getUserObject()).getElement() instanceof PsiFile && !(((PsiFile) ((TapestryNode) node.getUserObject()).getElement())
                    .getVirtualFile() instanceof VirtualFileImpl)) {
                canDelete = true;
            }

            // The element to delete is a folder node
            if (node.getUserObject() instanceof PackageNode && IdeaUtils.findFirstParent(node, LibrariesNode.class) == null) {
                canDelete = true;
            }

            if (!canDelete) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add in list the elements to delete
     *
     * @param child        the node to get the class and template to delete
     * @param elementsList the list for add the elements to delete
     * @return the list of elements to delete
     */
    public static List addElementToDelete(DefaultMutableTreeNode child, List<? super PsiElement> elementsList) {
        PsiFile elementClass = ((IntellijResource) ((PresentationLibraryElement) ((TapestryNode) child.getUserObject()).getElement()).getElementClass().getFile()).getPsiFile();

        elementsList.add(IdeaUtils.findPublicClass(elementClass));

        for (IResource template : ((PresentationLibraryElement) ((TapestryNode) child.getUserObject()).getElement()).getTemplate())
            elementsList.add(((IntellijResource) template).getPsiFile());

        for (IResource catalog : ((PresentationLibraryElement) ((TapestryNode) child.getUserObject()).getElement()).getMessageCatalog())
            elementsList.add(((IntellijResource) catalog).getPsiFile());

        return elementsList;
    }
}
