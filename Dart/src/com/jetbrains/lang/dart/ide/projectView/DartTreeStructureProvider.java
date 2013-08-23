package com.jetbrains.lang.dart.ide.projectView;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by fedorkorotkov.
 */
public class DartTreeStructureProvider implements TreeStructureProvider, DumbAware {
  private static final Condition<AbstractTreeNode> dartPackagesCondition = new Condition<AbstractTreeNode>() {
    @Override
    public boolean value(AbstractTreeNode node) {
      final Object value = node.getValue();
      VirtualFile virtualFile = value instanceof PsiDirectory ? ((PsiDirectory)value).getVirtualFile() : null;
      return virtualFile != null && isDartPackagesFolder(virtualFile);
    }

    private boolean isDartPackagesFolder(VirtualFile file) {
      return file.isDirectory() && file.is(VFileProperty.SYMLINK) && "packages".equalsIgnoreCase(file.getName());
    }
  };

  @Override
  public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings) {
    if (parent instanceof PsiDirectoryNode && ContainerUtil.find(children, dartPackagesCondition) != null) {
      return ContainerUtil.filter(children, Conditions.not(dartPackagesCondition));
    }
    return children;
  }

  @Nullable
  @Override
  public Object getData(Collection<AbstractTreeNode> selected, String dataName) {
    return null;
  }
}
