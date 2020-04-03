package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlatformioAction extends PlatformioActionBase {
  public PlatformioAction(@NotNull String text,
                          @Nullable String description) {
    super(text, description);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);

    Presentation presentation = e.getPresentation();
    if (presentation.isVisible() && ActionPlaces.isMainMenuOrActionSearch(e.getPlace())) {
      boolean enabled = false;
      Project project = e.getProject();
      if (project != null) {
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir != null && projectDir.findChild(PlatformioFileType.FILE_NAME) != null) {
          enabled = true;
        }
      }
      presentation.setEnabled(enabled);
    }
  }
}
