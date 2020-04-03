package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.tools.Tool;
import org.jetbrains.annotations.NotNull;

public class PlatformioCleanAction extends PlatformioAction {

  private static final String ARGUMENTS = "-c clion run --target clean";
  private static final String TEXT = "Clean";

  public PlatformioCleanAction() {super(TEXT, null);}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, ARGUMENTS, false, true);
  }

  public static Tool createPlatformioTool(Project project) {
    return createPlatformioTool(project, true, ARGUMENTS, TEXT);
  }
}
