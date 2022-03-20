package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.actionSystem.ActionPlaces.isMainMenuOrActionSearch;
import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE;
import static com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType.isFileOfType;
import static icons.ClionEmbeddedPlatformioIcons.Platformio;

public class PlatformioActionGroup extends ActionGroup {
  public PlatformioActionGroup() {
    super(ClionEmbeddedPlatformioBundle.message("platformio.actiongroup.name"), ClionEmbeddedPlatformioBundle.message("platformio.support"), Platformio);
  }

  @Override
  @NotNull
  public AnAction[] getChildren(final @Nullable AnActionEvent e) {
    return getChildren(e, ActionManager.getInstance());
  }

  @Override
  @NotNull
  public AnAction[] getChildren(final @Nullable AnActionEvent e, final @NotNull ActionManager manager) {
    return ((ActionGroup)manager.getAction("platformio-group")).getChildren(e, manager);
  }

  @Override
  public void update(final @NotNull AnActionEvent e) {
    super.update(e);
    final var presentation = e.getPresentation();
    if (presentation.isVisible() && !isMainMenuOrActionSearch(e.getPlace())) {
      final var file = e.getData(VIRTUAL_FILE);
      presentation.setEnabledAndVisible(isFileOfType(file));
    }
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }
}
