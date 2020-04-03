package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.concurrency.NonUrgentExecutor;
import com.intellij.util.containers.SmartHashSet;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class PlatformioListener implements BulkFileListener, ProjectManagerListener {
  @Override
  public void after(@NotNull List<? extends VFileEvent> events) {
    SmartHashSet<VirtualFile> affectedDirs = null;
    for (VFileEvent event : events) {
      VirtualFile file = event.getFile();
      if (file != null && file.getName().equals(PlatformioFileType.FILE_NAME)) {
        VirtualFile fileParent = file.getParent();
        if (affectedDirs == null) {
          affectedDirs = new SmartHashSet<>();
        }
        affectedDirs.add(fileParent);
      }
    }
    if (affectedDirs != null) {
      SmartHashSet<VirtualFile> affectedDirsHolder = affectedDirs;
      NonUrgentExecutor.getInstance().execute(() -> {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
          for (VirtualFile root : ProjectRootManager.getInstance(project).getContentRoots()) {
            if (affectedDirsHolder.contains(root)) {
              ServiceManager.getService(project, PlatformioService.class).enable(root.findChild(PlatformioFileType.FILE_NAME) != null);
            }
          }
        }
      });
    }
  }

  @Override
  public void projectOpened(@NotNull Project project) {
    boolean platformioFound = Stream.of(ProjectRootManager.getInstance(project).getContentRoots())
      .anyMatch(root -> root.findChild(PlatformioFileType.FILE_NAME) != null);
    if (platformioFound) {
      ServiceManager.getService(project, PlatformioService.class).enable(true);
    }
  }
}
