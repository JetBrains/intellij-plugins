package com.intellij.javascript.karma.coverage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoveragePluginMissingDialog {

  public static void showWarning(@Nullable Project project, @NotNull File karmaPackageDir) {
    File nodeModulesDir = karmaPackageDir.getParentFile();
    final String path;
    if ("node_modules".equals(nodeModulesDir.getName())) {
      path = nodeModulesDir.getAbsolutePath();
    }
    else {
      path = karmaPackageDir.getAbsolutePath();
    }
    String text = "<html><body>Node.js package \"karma-coverage\" is required for test coverage."
                  + "<div style=\"padding-top:3px\">To install it execute the following commands:</div>"
                  + "<pre><code>"
                  + "cd " + path + "\n"
                  + "npm install karma-coverage"
                  + "</code></pre>"
                  + "As the package is installed, run coverage again."
                  + "</body></html>";

    Messages.showWarningDialog(project, text, "Karma Coverage Plugin Missing");
  }

}
