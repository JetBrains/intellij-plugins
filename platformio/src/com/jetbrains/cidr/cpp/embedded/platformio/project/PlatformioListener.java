package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.openapi.ui.Messages.YES;
import static com.intellij.openapi.ui.Messages.showYesNoDialog;
import static com.intellij.util.PathUtilRt.getFileName;
import static com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType.FILE_NAME;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService.updateStateForProject;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.SourceTemplate.NONE;

public class PlatformioListener implements AsyncFileListener, StartupActivity {

  @Override
  public @Nullable ChangeApplier prepareChange(final @NotNull List<? extends VFileEvent> events) {
    boolean platformioAffected = false;
    for (final var event : events) {
      if (FILE_NAME.equals(getFileName(event.getPath())) || isPlatformioRename(event)) {
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
  public void runActivity(final @NotNull Project project) {
    final var state = updateStateForProject(project);
    if (state != PlatformioService.State.NONE) {
      final var platformioNonCmakeRoots = Stream.of(ProjectRootManager.getInstance(project).getContentRoots())
        .filter(root -> root.findChild(FILE_NAME) != null)
        .filter(root -> root.findChild("CMakeLists.txt") == null)
        .collect(Collectors.toList());
      if (!platformioNonCmakeRoots.isEmpty()) {
        boolean confirmCMakeCreate = showYesNoDialog(
          project,
          ClionEmbeddedPlatformioBundle.message("cmake.to.create.confirmation"),
          ClionEmbeddedPlatformioBundle.message("cmake.to.create.confirmation.title"), null) == YES;
        if (confirmCMakeCreate) {
          final var generator = new PlatformioProjectGenerator();
          platformioNonCmakeRoots.forEach(root -> generator.doGenerateProject(project, root, "", NONE));
        }
      }
    }
  }

  private static boolean isPlatformioRename(final @NotNull VFileEvent event) {
    if (event instanceof VFilePropertyChangeEvent) {
      final var changeEvent = (VFilePropertyChangeEvent) event;
      return changeEvent.isRename() && FILE_NAME.equals(getFileName(changeEvent.getNewPath()));
    }
    return false;
  }
}
