package com.intellij.javascript.karma.coverage;

import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.ExecutionConsoleEx;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageBadlyConfiguredConsole implements ExecutionConsoleEx {

  private final Project myProject;
  private final KarmaServer myServer;
  private final KarmaCoverageStartupStatus myStatus;
  private JComponent myComponent;

  public KarmaCoverageBadlyConfiguredConsole(@NotNull Project project,
                                             @NotNull KarmaServer server,
                                             @NotNull KarmaCoverageStartupStatus status) {
    myProject = project;
    myServer = server;
    myStatus = status;
  }

  @Override
  public void buildUi(RunnerLayoutUi ui) {
    registerTestRunTab(ui);
    registerKarmaServerTab(ui);
  }

  private void registerTestRunTab(@NotNull RunnerLayoutUi ui) {
    ui.getOptions().setMinimizeActionEnabled(false);
    final Content consoleContent = ui.createContent(ExecutionConsole.CONSOLE_CONTENT_ID,
                                                    getComponent(),
                                                    "Coverage Configuration Error",
                                                    AllIcons.Debugger.Console,
                                                    getPreferredFocusableComponent());
    ui.addContent(consoleContent, 1, PlaceInGrid.bottom, false);
    consoleContent.setCloseable(false);
    ui.selectAndFocus(consoleContent, false, false);
  }

  private void registerKarmaServerTab(RunnerLayoutUi ui) {
    KarmaServerLogComponent logComponent = new KarmaServerLogComponent(myProject, myServer, this);
    logComponent.installOn(ui);
  }

  @Nullable
  @Override
  public String getExecutionConsoleId() {
    return null;
  }

  @Override
  public JComponent getComponent() {
    if (myComponent == null) {
      myComponent = createTextPane();
    }
    return myComponent;
  }

  @NotNull
  private JTextPane createTextPane() {
    JTextPane textPane = new JTextPane();
    String text = getWarningMessage();
    Messages.configureMessagePaneUi(textPane, text);
    return textPane;
  }

  @NotNull
  private String getWarningMessage() {
    if (!myStatus.isCoverageReporterSpecifiedInConfig()) {
      return getWarningAboutMissingCoverageReporterInConfigFile();
    }
    if (!myStatus.isCoverageReportFound()) {
      if (!myStatus.isCoveragePluginInstalled()) {
        return getSuggestionAboutCoveragePluginInstallation();
      }
      return getWarningAboutMissingCoveragePluginInConfigFile();
    }
    return "";
  }

  @NotNull
  private static String getWarningAboutMissingCoverageReporterInConfigFile() {
    String[] program = new String[] {
      "module.exports = function (config) {",
      "  config.set({",
      "    ...",
      "    reporters: [..., 'coverage', ...],",
      "    ...",
      "  });",
      "};"
    };

    return "<html><body>"
           + "Make sure <code>'coverage'</code> reporter is specified in config file:"
           + "<pre><code>"
           + StringUtil.join(program, "\n")
           + "</code></pre>"
           + "</body></html>";
  }

  @NotNull
  private String getSuggestionAboutCoveragePluginInstallation() {
    File karmaPackageDir = myServer.getKarmaPackageDir();
    File nodeModulesDir = karmaPackageDir.getParentFile();
    final String path;
    if ("node_modules".equals(nodeModulesDir.getName())) {
      path = nodeModulesDir.getAbsolutePath();
    }
    else {
      path = karmaPackageDir.getAbsolutePath();
    }
    return "<html><body>Node.js package \"karma-coverage\" is required for test coverage."
           + "<div style='padding-top:3px'>To install it execute the following commands:</div>"
           + "<pre><code>"
           + "cd " + path + "\n"
           + "npm install karma-coverage"
           + "</code></pre>"
           + "As the package is installed, run coverage again."
           + "</body></html>";
  }

  @NotNull
  private static String getWarningAboutMissingCoveragePluginInConfigFile() {
    String[] program = new String[] {
      "module.exports = function (config) {",
      "  config.set({",
      "    ...",
      "    plugins: [..., 'karma-coverage', ...],",
      "    ...",
      "  });",
      "};"
    };
    return "<html><body>"
           + "Implementation of <code>'coverage'</code> reporter is not found.<br/>"
           + "<div style='padding-top:3px; padding-bottom:3px\' >"
           +   "Make sure <code>'karma-coverage'</code> plugin is specified." +
           " </div>"
           + "<pre><code>"
           + StringUtil.join(program, "\n")
           + "</code></pre>"
           + "</body></html>";
  }

  @Override
  public JComponent getPreferredFocusableComponent() {
    return null;
  }

  @Override
  public void dispose() {
  }

}
