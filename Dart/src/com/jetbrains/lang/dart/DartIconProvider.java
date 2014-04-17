package com.jetbrains.lang.dart;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IconProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.PlatformIcons;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartIconProvider extends IconProvider {

  public static final Icon FOLDER_SYMLINK_ICON = LayeredIcon.create(PlatformIcons.FOLDER_ICON, PlatformIcons.SYMLINK_ICON);

  @Nullable
  public Icon getIcon(@NotNull final PsiElement element, @Iconable.IconFlags final int flags) {
    if (element instanceof PsiDirectory) {
      if (isFolderNearPubspecYaml(((PsiDirectory)element).getVirtualFile(), "build")) return AllIcons.Modules.GeneratedFolder;
      if (isFolderNearPubspecYaml(((PsiDirectory)element).getVirtualFile(), "web")) return AllIcons.Nodes.WebFolder;
      if (isFolderNearPubspecYaml(((PsiDirectory)element).getVirtualFile(), "test")) return AllIcons.Modules.TestSourceFolder;
      if (isFolderNearPubspecYaml(((PsiDirectory)element).getVirtualFile(), "packages")) return DartIcons.Package_root;
      if (isFolderNearPubspecYaml(((PsiDirectory)element).getVirtualFile().getParent(), "packages")) return FOLDER_SYMLINK_ICON;
    }

    return null;
  }

  private static boolean isFolderNearPubspecYaml(final @Nullable VirtualFile folder, final @NotNull String folderName) {
    if (folder != null && folder.isDirectory() && folder.isInLocalFileSystem() && folderName.equals(folder.getName())) {
      final VirtualFile parentFolder = folder.getParent();
      final VirtualFile pubspecYamlFile = parentFolder != null ? parentFolder.findChild(PUBSPEC_YAML) : null;
      return pubspecYamlFile != null;
    }
    return false;
  }
}
