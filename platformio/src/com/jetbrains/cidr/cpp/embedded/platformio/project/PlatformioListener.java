package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.util.PathUtilRt;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlatformioListener implements AsyncFileListener, StartupActivity {

  @Override
  public @Nullable ChangeApplier prepareChange(@NotNull List<? extends @NotNull VFileEvent> events) {
    boolean platformioAffected = false;
    for (VFileEvent event : events) {
      String path = event.getPath();
      if (PlatformioFileType.FILE_NAME.equals(PathUtilRt.getFileName(path)) ||
          isPlatformioRename(event)) {
        platformioAffected = true;
        break;
      }
    }
    if (!platformioAffected) return null;
    return new ChangeApplier() {
      @Override
      public void afterVfsChange() {
        Stream.of(ProjectManager.getInstance().getOpenProjects()).forEach(PlatformioService::updateStateForProject);
      }
    };
  }

  @Override
  public void runActivity(@NotNull Project project) {
    PlatformioService.State state = PlatformioService.updateStateForProject(project);
    if (state != PlatformioService.State.NONE) {
      List<VirtualFile> platformioNonCmakeRoots = Stream.of(ProjectRootManager.getInstance(project).getContentRoots())
        .filter(root -> root.findChild(PlatformioFileType.FILE_NAME) != null)
        .filter(root -> root.findChild("CMakeLists.txt") == null).toList();
      if (!platformioNonCmakeRoots.isEmpty()) {
        boolean confirmCMakeCreate = Messages.showYesNoDialog(
          project,
          ClionEmbeddedPlatformioBundle.message("cmake.to.create.confirmation"),
          ClionEmbeddedPlatformioBundle.message("cmake.to.create.confirmation.title"), null) == Messages.YES;
        if (confirmCMakeCreate) {
          PlatformioProjectGenerator generator = new PlatformioProjectGenerator();
          platformioNonCmakeRoots.forEach(root -> generator.doGenerateProject(project, root, "", SourceTemplate.NONE));
        }
      }
    }
  }

  private static boolean isPlatformioRename(VFileEvent event) {
    if (event instanceof VFilePropertyChangeEvent) {
      VFilePropertyChangeEvent changeEvent = (VFilePropertyChangeEvent)event;
      return changeEvent.isRename() &&
             PlatformioFileType.FILE_NAME.equals(PathUtilRt.getFileName(changeEvent.getNewPath()));
    }
    return false;
  }
}
