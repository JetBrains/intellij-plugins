package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlatformioActionGroup extends ActionGroup {
  public static final PlatformioAction CLEAN_ACTION;
  private static final AnAction[] ourActions;

  static {
    long executionId = ExecutionEnvironment.getNextUnusedExecutionId();
    CLEAN_ACTION = new PlatformioAction(executionId, "Clean", null, "-c clion run --target clean", false, true);
    ourActions = new AnAction[]{
      new PlatformioAction(executionId, "Build", null, "-c clion run --target debug", false, true),
      new PlatformioAction(executionId, "Build Production", null, "-c clion run", false, true),
      CLEAN_ACTION,
      new PlatformioAction(executionId, "Check", null, "-c clion check", false, true),
      Separator.create(),
      new PlatformioAction(executionId, "Re-Init", "(Re)initialize project & CMake", "-c clion init --ide clion", true, false),
      new PlatformioAction(executionId, "Update All", "Update platforms, toolchains etc", "-c clion update", true, false),
      Separator.create(),
      new PlatformioAction(executionId, "Monitor", null, "-c clion device monitor", false, true),
      new PlatformioActionBase(executionId, "Home", "Start PlatformIO internal web server", "-c clion home", false, false)
    };
  }

  public PlatformioActionGroup() {
    //noinspection DialogTitleCapitalization
    super("PlatformIO", "PlatformIO Support", PlatformioFileType.ICON);
  }

  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    return ourActions;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();
    if (presentation.isVisible() && !ActionPlaces.isMainMenuOrActionSearch(e.getPlace())) {
      VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
      if (file != null && FileTypeRegistry.getInstance().isFileOfType(file, PlatformioFileType.INSTANCE)) {
        presentation.setEnabledAndVisible(true);
      }
      else {
        presentation.setEnabledAndVisible(false);
      }
    }
  }
}
