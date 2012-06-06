package com.intellij.lang.javascript.flex;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.WritingAccessProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * @author ksafonov
 */
public class FlashWritingAccessProvider extends WritingAccessProvider {

  private final Project myProject;

  public FlashWritingAccessProvider(Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public Collection<VirtualFile> requestWriting(VirtualFile... files) {
    return Collections.emptyList();
  }

  @Override
  public boolean isPotentiallyWritable(@NotNull VirtualFile file) {
    FileType fileType = file.getFileType();
    if (ActionScriptFileType.INSTANCE != fileType &&
        FlexApplicationComponent.MXML != fileType &&
        FlexApplicationComponent.SWF_FILE_TYPE != fileType &&
        !"swc".equalsIgnoreCase(file.getExtension())) {
      return true;
    }

    // protect SDK and library sources
    VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(file);
    if (jarRoot != null) {
      file = jarRoot;
    }
    ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    if ((fileIndex.isInLibrarySource(file) || fileIndex.isInLibraryClasses(file)) && !fileIndex.isInContent(file)) {
      return false;
    }
    return true;
  }
}
