package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.view.TapestryProjectViewPane;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

import java.util.TreeSet;

/**
 * Tapestry module node.
 */
public class ModuleNode extends AbstractModuleNode {

    public ModuleNode(@NotNull final Module module, final AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleNode @NotNull [] getChildren() {
        final TreeSet<TapestryNode> children = new TreeSet<>(PackageNodesComparator.getInstance());

        final ModuleFileIndex moduleFileIndex = ModuleRootManager.getInstance((Module) getElement()).getFileIndex();
        moduleFileIndex.iterateContent(
          virtualfile -> {
              if (virtualfile.isDirectory() && moduleFileIndex.isInSourceContent(virtualfile)) {
                  PsiDirectory psiDirectory = PsiManager.getInstance(myProject).findDirectory(virtualfile);

                  PsiPackage aPackage = IdeaUtils.getPackage(psiDirectory);
                  if (TapestryProjectViewPane.getInstance(myProject).isFromBasePackage()) {
                    if (aPackage.getName() != null && aPackage.getQualifiedName()
                            .equals(TapestryModuleSupportLoader.getTapestryProject(_module).getApplicationLibrary().getBasePackage())) {
                        children.add(new LibraryNode(TapestryModuleSupportLoader.getTapestryProject(_module).getApplicationLibrary(), psiDirectory, _module, _treeBuilder));
                    }
                  } else if (aPackage.getName() != null
                          && (aPackage.getParentPackage() == null || aPackage.getParentPackage().getName() == null)) {
                      children.add(new PackageNode(PsiManager.getInstance(myProject).findDirectory(virtualfile), (Module) getElement(), _treeBuilder));
                  }
              }
              return true;
          }
        );

        if (TapestryProjectViewPane.getInstance(myProject).isShowLibraries()) {
            children.add(new LibrariesNode(_module, _treeBuilder));
        }

        return children.toArray(new TapestryNode[0]);
    }
}
