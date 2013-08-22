package com.intellij.javascript.karma.coverage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageWarnings {

  public static void suggestCoveragePluginInstallation(@Nullable Project project, @NotNull File karmaPackageDir) {
    File nodeModulesDir = karmaPackageDir.getParentFile();
    final String path;
    if ("node_modules".equals(nodeModulesDir.getName())) {
      path = nodeModulesDir.getAbsolutePath();
    }
    else {
      path = karmaPackageDir.getAbsolutePath();
    }
    String text = "<html><body>Node.js package \"karma-coverage\" is required for test coverage."
                  + "<div style='padding-top:3px'>To install it execute the following commands:</div>"
                  + "<pre><code>"
                  + "cd " + path + "\n"
                  + "npm install karma-coverage"
                  + "</code></pre>"
                  + "As the package is installed, run coverage again."
                  + "</body></html>";

    Messages.showWarningDialog(project, text, "Karma Coverage");
  }

  public static void warnAboutMissingCoverageReporterInConfigFile(@NotNull Project project) {
    String[] program = new String[] {
      "module.exports = function (config) {",
      "  config.set({",
      "    ...",
      "    reporters: [..., 'coverage', ...],",
      "    ...",
      "  });",
      "};"
    };

    String text = "<html><body>"
                  + "Make sure <code>'coverage'</code> reporter is specified in config file:"
                  + "<pre><code>"
                  + StringUtil.join(program, "\n")
                  + "</code></pre>"
                  + "</body></html>";
    Messages.showWarningDialog(project, text, "Karma Coverage");
  }

  public static void warnAboutMissingCoveragePluginInConfigFile(Project project) {
    String[] program = new String[] {
      "module.exports = function (config) {",
      "  config.set({",
      "    ...",
      "    plugins: [..., 'karma-coverage', ...],",
      "    ...",
      "  });",
      "};"
    };
    String text = "<html><body>"
                  + "Implementation of <code>'coverage'</code> reporter is not found.<br/>"
                  + "<div style='padding-top:3px; padding-bottom:3px\' >"
                  +   "Make sure <code>'karma-coverage'</code> plugin is specified." +
                  " </div>"
                  + "<pre><code>"
                  + StringUtil.join(program, "\n")
                  + "</code></pre>"
                  + "</body></html>";
    Messages.showWarningDialog(project, text, "Karma Coverage");
  }

}
