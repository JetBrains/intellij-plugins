package com.jetbrains.lang.dart;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.PlatformIcons;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class DartTreeStructureProvider implements TreeStructureProvider {

  @NotNull
  public Collection<AbstractTreeNode> modify(final @NotNull AbstractTreeNode parentNode,
                                             final @NotNull Collection<AbstractTreeNode> children,
                                             final ViewSettings settings) {
    // root/packages/ThisProject folder is excluded in dart projects (see DartProjectComponent.excludePackagesFolders),
    // this provider shows empty node instead with name "ThisProject (link to 'lib' folder)"
    final VirtualFile file = parentNode instanceof PsiDirectoryNode ? ((PsiDirectoryNode)parentNode).getVirtualFile() : null;
    final VirtualFile parentFolder = file != null && file.isDirectory() && "packages".equals(file.getName()) ? file.getParent() : null;
    final VirtualFile pubspecYamlFile = parentFolder == null ? null : parentFolder.findChild(PubspecYamlUtil.PUBSPEC_YAML);
    final String pubspecName = pubspecYamlFile == null ? null : PubspecYamlUtil.getPubspecName(pubspecYamlFile);

    if (pubspecName != null && file.findChild(pubspecName) != null && !containsFolderNode(children, pubspecName)) {
      final ArrayList<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>(children);
      result.add(new SymlinkToLibFolderNode(parentNode.getProject(), pubspecName));
      return result;
    }

    return children;
  }

  private static boolean containsFolderNode(final @NotNull Collection<AbstractTreeNode> nodes, final @NotNull String folderName) {
    for (AbstractTreeNode node : nodes) {
      final VirtualFile file = node instanceof PsiDirectoryNode ? ((PsiDirectoryNode)node).getVirtualFile() : null;
      if (file != null && folderName.equals(file.getName())) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public Object getData(final Collection<AbstractTreeNode> selected, final String dataName) {
    return null;
  }

  private static class SymlinkToLibFolderNode extends AbstractTreeNode<String> {
    public SymlinkToLibFolderNode(final Project project, final String pubspecName) {
      super(project, pubspecName);
      myName = pubspecName;
      setIcon(LayeredIcon.create(PlatformIcons.FOLDER_ICON, PlatformIcons.SYMLINK_ICON));
    }

    @NotNull
    public Collection<? extends AbstractTreeNode> getChildren() {
      return Collections.emptyList();
    }

    protected void update(final PresentationData presentation) {
      presentation.setIcon(getIcon());
      presentation.setPresentableText(myName);
      presentation.setLocationString(DartBundle.message("link.to.lib.folder"));
    }

    public int getWeight() {
      return 0;
    }
  }
}
