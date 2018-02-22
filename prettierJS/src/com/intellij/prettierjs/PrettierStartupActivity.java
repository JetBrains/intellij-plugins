package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class PrettierStartupActivity implements StartupActivity {
  @Override
  public void runActivity(@NotNull Project project) {
    Application application = ApplicationManager.getApplication();

    if (!application.isUnitTestMode()) {
      application.executeOnPooledThread(() -> {
        NodePackage nodePackage = PrettierConfiguration.getInstance(project).getOrDetectNodePackage();

        if (nodePackage != null && !nodePackage.isEmptyPath() && nodePackage.isValid()) {
          PrettierLanguageService.getInstance(project).initSupportedFiles(nodePackage);
        }
      });
    }
  }
}
