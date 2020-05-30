// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesNode;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElement;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElementNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartPackagesLibraryType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;
import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartTreeStructureProvider implements TreeStructureProvider, DumbAware {

  @Override
  @NotNull
  public Collection<AbstractTreeNode<?>> modify(final @NotNull AbstractTreeNode<?> parentNode,
                                             final @NotNull Collection<AbstractTreeNode<?>> children,
                                             final ViewSettings settings) {
    if (parentNode instanceof ExternalLibrariesNode) {
      return ContainerUtil.map(children, node -> {
        if (node instanceof NamedLibraryElementNode &&
            (DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME.equals(node.getName()) ||
             DartSdk.DART_SDK_LIB_NAME.equals(node.getName()))) {
          final boolean isSdkRoot = DartSdk.DART_SDK_LIB_NAME.equals(node.getName());

          NamedLibraryElement value = ((NamedLibraryElementNode)node).getValue();
          if (value != null) {
            return new NamedLibraryElementNode(node.getProject(), value, settings) {
              @Override
              public boolean canNavigate() {
                return isSdkRoot; // no sense to navigate anywhere in case of "Dart Packages" library
              }

              @Override
              public void navigate(boolean requestFocus) {
                final Project project = getProject();
                if (project != null) {
                  DartConfigurable.openDartSettings(project);
                }
              }
            };
          }
        }

        return node;
      });
    }

    if (parentNode instanceof NamedLibraryElementNode &&
        (DartPackagesLibraryType.DART_PACKAGES_LIBRARY_NAME.equals(parentNode.getName()) ||
         DartSdk.DART_SDK_LIB_NAME.equals(parentNode.getName()))) {
      final boolean isSdkRoot = DartSdk.DART_SDK_LIB_NAME.equals(parentNode.getName());

      return ContainerUtil.map(children, node -> {
        final VirtualFile dir = node instanceof PsiDirectoryNode ? ((PsiDirectoryNode)node).getVirtualFile() : null;
        if (dir != null && dir.isInLocalFileSystem() && dir.isDirectory() && (isSdkRoot || "lib".equals(dir.getName()))) {
          return new DartSdkOrLibraryRootNode(node.getProject(), ((PsiDirectoryNode)node).getValue(), settings);
        }

        return node;
      });
    }

    // root/packages/ThisProject and root/packages/PathPackage folders are excluded in dart projects (see DartStartupActivity.excludeBuildAndPackagesFolders),
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
      final ArrayList<AbstractTreeNode<?>> modifiedChildren = new ArrayList<>(children);

      final DartUrlResolver resolver = DartUrlResolver.getInstance(project, pubspecYamlFile);
      resolver.processLivePackages((packageName, packageDir) -> {
        final VirtualFile folder = packagesDir.findChild(packageName);
        if (folder != null) {
          final AbstractTreeNode node = getFolderNode(children, folder);
          if (node == null) {
            modifiedChildren.add(new SymlinkToLivePackageNode(project, packageName, packageDir));
          }
          else {
            node.getPresentation().setLocationString(getPackageLocationString(packageDir));
          }
        }
      });

      return modifiedChildren;
    }

    return children;
  }

  @Nullable
  private static AbstractTreeNode getFolderNode(final @NotNull Collection<AbstractTreeNode<?>> nodes, final @NotNull VirtualFile folder) {
    for (AbstractTreeNode node : nodes) {
      if (node instanceof PsiDirectoryNode && folder.equals(((PsiDirectoryNode)node).getVirtualFile())) {
        return node;
      }
    }
    return null;
  }

  private static String getPackageLocationString(@NotNull final VirtualFile packageDir) {
    final String path = packageDir.getPath();
    final int lastSlashIndex = path.lastIndexOf("/");
    final int prevSlashIndex = lastSlashIndex == -1 ? -1 : path.substring(0, lastSlashIndex).lastIndexOf("/");
    return FileUtil.toSystemDependentName(prevSlashIndex < 0 ? path : path.substring(prevSlashIndex + 1));
  }

  private static class SymlinkToLivePackageNode extends AbstractTreeNode<String> {
    @NotNull private final String mySymlinkPath;

    SymlinkToLivePackageNode(final @NotNull Project project,
                                    final @NotNull String packageName,
                                    final @NotNull VirtualFile packageDir) {
      super(project, packageName);
      myName = packageName;
      mySymlinkPath = getPackageLocationString(packageDir);
      setIcon(DartIconProvider.EXCLUDED_FOLDER_SYMLINK_ICON);
    }

    @Override
    @NotNull
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
      return Collections.emptyList();
    }

    @Override
    protected void update(@NotNull final PresentationData presentation) {
      presentation.setIcon(getIcon());
      presentation.setPresentableText(myName);
      presentation.setLocationString(mySymlinkPath);
    }

    @Override
    public int getWeight() {
      return 0;
    }
  }

  private static class DartSdkOrLibraryRootNode extends PsiDirectoryNode {
    DartSdkOrLibraryRootNode(final Project project, @NotNull PsiDirectory value, final ViewSettings settings) {
      super(project, value, settings);
    }

    @Override
    public boolean canNavigate() {
      return false; // 'Dart Packages' and 'Dart SDK' libraries are generated automatically, no need to navigate to Project Structure
    }

    @Override
    public boolean canNavigateToSource() {
      return false;
    }

    @Override
    protected void updateImpl(@NotNull final PresentationData data) {
      super.updateImpl(data);

      final VirtualFile dir = getVirtualFile();
      final VirtualFile parentDir = dir == null ? null : dir.getParent();
      if (parentDir != null && parentDir.isInLocalFileSystem() && dir.isDirectory() && "lib".equals(dir.getName())) {
        data.setPresentableText(parentDir.getName()); // e.g. "path-1.3.6" instead of "lib"
      }

      data.setLocationString(""); // we do not want 'library root' location string
    }
  }
}
