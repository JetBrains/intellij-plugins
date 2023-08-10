// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.projectView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IconProvider;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.LayeredIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public final class DartIconProvider extends IconProvider {
  public static final Icon FOLDER_SYMLINK_ICON = LayeredIcon.create(AllIcons.Nodes.Folder, AllIcons.Nodes.Symlink);
  public static final Icon EXCLUDED_FOLDER_SYMLINK_ICON = LayeredIcon.create(AllIcons.Modules.ExcludeRoot, AllIcons.Nodes.Symlink);

  @Override
  public @Nullable Icon getIcon(final @NotNull PsiElement element, @Iconable.IconFlags final int flags) {
    if (element instanceof PsiDirectory) {
      final VirtualFile folder = ((PsiDirectory)element).getVirtualFile();

      if (isFolderNearPubspecYaml(folder, "build")) {
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(element.getProject()).getFileIndex();
        return fileIndex.isExcluded(folder) ? AllIcons.Modules.ExcludedGeneratedRoot
                                            : AllIcons.Modules.GeneratedFolder;
      }
      if (isFolderNearPubspecYaml(folder, "web")) return AllIcons.Nodes.WebFolder;
      if (isFolderNearPubspecYaml(folder, "test")) return AllIcons.Nodes.TestSourceFolder;
      if (isFolderNearPubspecYaml(folder, "tool")) return AllIcons.Nodes.KeymapTools;
      if (isFolderNearPubspecYaml(folder.getParent(), "packages")) {
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(element.getProject()).getFileIndex();
        return fileIndex.isExcluded(folder) ? EXCLUDED_FOLDER_SYMLINK_ICON
                                            : FOLDER_SYMLINK_ICON;
      }
    }

    return null;
  }

  public static boolean isFolderNearPubspecYaml(@Nullable VirtualFile folder, @NotNull String folderName) {
    if (folder != null && folder.isDirectory() && folder.isInLocalFileSystem() && folderName.equals(folder.getName())) {
      final VirtualFile parentFolder = folder.getParent();
      final VirtualFile pubspecYamlFile = parentFolder != null ? parentFolder.findChild(PUBSPEC_YAML) : null;
      return pubspecYamlFile != null;
    }
    return false;
  }
}
