package com.intellij.lang.javascript.flex;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JdkOrderEntry;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vcs.readOnlyHandler.WritingAccessProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author ksafonov
 */
public class FlexWritingAccessProvider implements WritingAccessProvider {

  private final Project myProject;

  public FlexWritingAccessProvider(Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public Collection<VirtualFile> requestWriting(VirtualFile... files) {
    return ContainerUtil.filter(files, new Condition<VirtualFile>() {
      @Override
      public boolean value(VirtualFile virtualFile) {
        return isReadonly(virtualFile);
      }
    });
  }

  private boolean isReadonly(VirtualFile file) {
    FileType fileType = file.getFileType();

    // protect SWF-s until somebody fixes IDEA-68156
    if (FlexApplicationComponent.SWF_FILE_TYPE == fileType) {
      return true;
    }

    // protect SDK and library sources
    if (ActionScriptFileType.INSTANCE == fileType || FlexApplicationComponent.MXML == fileType) {
      List<OrderEntry> entriesForFile = ProjectRootManager.getInstance(myProject).getFileIndex().getOrderEntriesForFile(file);
      boolean readonly = false;
      for (OrderEntry orderEntry : entriesForFile) {
        if (orderEntry instanceof JdkOrderEntry || orderEntry instanceof LibraryOrderEntry) {
          readonly = true;
        }
        else {
          return false;
        }
      }
      return readonly;
    }

    return false;
  }
}
