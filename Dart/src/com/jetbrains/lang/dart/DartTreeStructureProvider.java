package com.jetbrains.lang.dart;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;
import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartTreeStructureProvider implements TreeStructureProvider {

  @NotNull
  public Collection<AbstractTreeNode> modify(final @NotNull AbstractTreeNode parentNode,
                                             final @NotNull Collection<AbstractTreeNode> children,
                                             final ViewSettings settings) {
    // root/packages/ThisProject and root/packages/PathPackage folders are excluded in dart projects (see DartProjectComponent.excludePackagesFolders),
    // this provider adds location string tho these nodes in Project View like "ThisProject (ThisProject/lib)"
    final Project project = parentNode.getProject();
    final VirtualFile packagesDir = parentNode instanceof PsiDirectoryNode && project != null
                                    ? ((PsiDirectoryNode)parentNode).getVirtualFile()
                                    : null;
    final VirtualFile parentFolder = packagesDir != null && packagesDir.isDirectory() && PACKAGES_FOLDER_NAME.equals(packagesDir.getName())
                                     ? packagesDir.getParent()
                                     : null;
    final VirtualFile pubspecYamlFile = parentFolder != null
                                        ? parentFolder.findChild(PUBSPEC_YAML)
                                        : null;

    if (pubspecYamlFile != null && !pubspecYamlFile.isDirectory()) {
      final ArrayList<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>(children);

      final DartUrlResolver resolver = DartUrlResolver.getInstance(project, pubspecYamlFile);
      resolver.processLivePackages(new PairConsumer<String, VirtualFile>() {
        public void consume(final @NotNull String packageName, final @NotNull VirtualFile packageDir) {
          final AbstractTreeNode node = getFolderNode(children, packageName);
          if (node != null) {
            node.getPresentation().setLocationString(getLocationString(packageDir));
          }
        }
      });

      return result;
    }

    return children;
  }

  @Nullable
  private static AbstractTreeNode getFolderNode(final @NotNull Collection<AbstractTreeNode> nodes, final @NotNull String folderName) {
    for (AbstractTreeNode node : nodes) {
      if (node instanceof PsiDirectoryNode) {
        final VirtualFile folder = ((PsiDirectoryNode)node).getVirtualFile();
        if (folder != null && folder.getName().equals(folderName)) {
          return node;
        }
      }
    }
    return null;
  }

  private static String getLocationString(VirtualFile packageDir) {
    final String path = packageDir.getPath();
    final int lastSlashIndex = path.lastIndexOf("/");
    final int prevSlashIndex = lastSlashIndex == -1 ? -1 : path.substring(0, lastSlashIndex).lastIndexOf("/");
    return FileUtil.toSystemDependentName(prevSlashIndex < 0 ? path : path.substring(prevSlashIndex + 1));
  }

  @Nullable
  public Object getData(final Collection<AbstractTreeNode> selected, final String dataName) {
    return null;
  }
}
