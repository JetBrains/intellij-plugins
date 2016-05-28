package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartWritingAccessProvider implements NonProjectFileWritingAccessExtension {

  private final Project myProject;

  public DartWritingAccessProvider(Project project) {
    myProject = project;
  }

  @Override
  public boolean isNotWritable(@NotNull VirtualFile file) {
    return isInDartSdkOrDartPackagesFolder(myProject, file);
  }

  public static boolean isInDartSdkOrDartPackagesFolder(final @NotNull PsiFile psiFile) {
    final VirtualFile vFile = psiFile.getOriginalFile().getVirtualFile();
    return vFile != null && isInDartSdkOrDartPackagesFolder(psiFile.getProject(), vFile);
  }

  public static boolean isInDartSdkOrDartPackagesFolder(final @NotNull Project project, final @NotNull VirtualFile file) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

    if (fileIndex.isInLibraryClasses(file) && !fileIndex.isInContent(file)) {
      return true; // file in SDK or in custom package root
    }

    if (fileIndex.isExcluded(file) || (fileIndex.isInContent(file) && isInDartPackagesFolder(fileIndex, file))) {
      return true; // symlinked child of 'packages' folder. Real location is in user cache folder for Dart packages, not in project
    }

    return false;
  }

  private static boolean isInDartPackagesFolder(final ProjectFileIndex fileIndex, final VirtualFile file) {
    VirtualFile parent = file;
    while ((parent = parent.getParent()) != null && fileIndex.isInContent(parent)) {
      if (DartUrlResolver.PACKAGES_FOLDER_NAME.equals(parent.getName())) {
        return VfsUtilCore.findRelativeFile("../" + PUBSPEC_YAML, parent) != null;
      }
    }

    return false;
  }
}
