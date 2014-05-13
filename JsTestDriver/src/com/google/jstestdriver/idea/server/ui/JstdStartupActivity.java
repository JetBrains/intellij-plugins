package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.execution.JstdSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

public class JstdStartupActivity implements StartupActivity {

  @Override
  public void runActivity(@NotNull final Project project) {
    boolean showToolWindow = JstdSettingsUtil.areJstdConfigFilesInProject(project);
    if (showToolWindow) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          JstdToolWindowManager.getInstance(project).setAvailable(true);
        }
      });
    }
  }
}
