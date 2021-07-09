package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType;
import icons.ClionEmbeddedPlatformioIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlatformioActionGroup extends ActionGroup {
  public PlatformioActionGroup() {
    super(ClionEmbeddedPlatformioBundle.message("platformio.actiongroup.name"), ClionEmbeddedPlatformioBundle.message("platformio.support"),
          ClionEmbeddedPlatformioIcons.Platformio);
  }

  @Override
  public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
    return getChildren(e, ActionManager.getInstance());
  }

  @Override
  public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e, @NotNull ActionManager manager) {
    return ((ActionGroup)manager.getAction("platformio-group")).getChildren(e, manager);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();
    if (presentation.isVisible() && !ActionPlaces.isMainMenuOrActionSearch(e.getPlace())) {
      VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
      if (PlatformioFileType.isFileOfType(file)) {
        presentation.setEnabledAndVisible(true);
      }
      else {
        presentation.setEnabledAndVisible(false);
      }
    }
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }
}
