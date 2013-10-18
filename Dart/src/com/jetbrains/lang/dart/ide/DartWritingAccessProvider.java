package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.WritingAccessProvider;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class DartWritingAccessProvider extends WritingAccessProvider {

  private final Project myProject;

  public DartWritingAccessProvider(Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public Collection<VirtualFile> requestWriting(VirtualFile... files) {
    return Collections.emptyList();
  }

  @Override
  public boolean isPotentiallyWritable(@NotNull VirtualFile file) {
    if (DartFileType.INSTANCE != file.getFileType()) return true;
    ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    
    if (!fileIndex.isInContent(file) && (fileIndex.isInLibrarySource(file) || fileIndex.isInLibraryClasses(file))) {
      return false; // file in SDK
    }
    
    if (fileIndex.isInContent(file) && isInDartPackagesFolder(fileIndex, file)) {
      return false; // symlinked child of 'packages' folder. Real location is in user cache folder for Dart packages, not in project
    }
    
    return true;
  }

  private static boolean isInDartPackagesFolder(final ProjectFileIndex fileIndex, final VirtualFile file) {
    // todo check Windows junctions when supported
    if (!SystemInfo.isWindows && !file.is(VFileProperty.SYMLINK)) return false;
    
    VirtualFile parent = file;
    while((parent = parent.getParent()) != null && fileIndex.isInContent(parent)) {
      if ("packages".equals(parent.getName())) return true;
    }
    
    return false;
  }
}
