package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.tools.Tool;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.CLEAN;

public class PlatformioCleanAction extends PlatformioAction {

  private static final String ARGUMENTS = "-c clion run --target clean";
  private static final Supplier<String> TEXT = () -> ClionEmbeddedPlatformioBundle.message("task.clean");

  public PlatformioCleanAction() {super(TEXT);}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, ARGUMENTS, false, true);
  }

  @Override
  public @NotNull FUS_COMMAND getFusCommand() {
    return CLEAN;
  }

  public static Tool createPlatformioTool(Project project) {
    return createPlatformioTool(project, true, ARGUMENTS, TEXT.get());
  }
}
