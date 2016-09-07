package com.intellij.javascript.karma.coverage;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.ExecutionConsoleEx;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.openapi.editor.colors.*;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ComponentsKt;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.layout.LayoutKt;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageConfigurationErrorConsole implements ExecutionConsoleEx {

  private static final String TITLE = "<div style='padding-bottom:4px;'><strong>Sorry, looks like we can't measure code coverage!</strong></div>";
  private static final String SEE_KARMA_SERVER_TAB = "<div style='padding-top:3px;'>See 'Karma Server' tab for details.</div>";

  private final Project myProject;
  private final KarmaServer myServer;
  private final KarmaCoverageStartupStatus myStatus;
  private JComponent myComponent;

  public KarmaCoverageConfigurationErrorConsole(@NotNull Project project,
                                                @NotNull KarmaServer server,
                                                @Nullable KarmaCoverageStartupStatus status) {
    myProject = project;
    myServer = server;
    myStatus = status;
  }

  @Override
  public void buildUi(RunnerLayoutUi ui) {
    registerTestRunTab(ui);
    // select 'Karma Server' tab if karma server is starting
    KarmaServerLogComponent.register(myProject, myServer, ui, myStatus == null);
  }

  private void registerTestRunTab(@NotNull RunnerLayoutUi ui) {
    ui.getOptions().setMinimizeActionEnabled(false);
    Content consoleContent = ui.createContent(ExecutionConsole.CONSOLE_CONTENT_ID,
                                              getComponent(),
                                              myStatus != null ? "Coverage Configuration Error" : "Test Run",
                                              AllIcons.Debugger.Console,
                                              getPreferredFocusableComponent());
    ui.addContent(consoleContent, 1, PlaceInGrid.bottom, false);
    consoleContent.setCloseable(false);
    ui.selectAndFocus(consoleContent, false, false);
  }

  @Nullable
  @Override
  public String getExecutionConsoleId() {
    return null;
  }

  @Override
  public JComponent getComponent() {
    if (myComponent == null) {
      JEditorPane pane = createEditorPane();
      pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      myComponent = ScrollPaneFactory.createScrollPane(pane,
                                                       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    return myComponent;
  }

  @NotNull
  private JEditorPane createEditorPane() {
    EditorColorsScheme colorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
    Font font = colorsScheme.getFont(EditorFontType.PLAIN);
    Color background = colorsScheme.getDefaultBackground();
    Color foreground = getTextForeground(colorsScheme);
    return ComponentsKt.htmlComponent(getWarningMessage(), font, background, foreground);
  }

  @NotNull
  private static Color getTextForeground(@NotNull EditorColorsScheme colorsScheme) {
    TextAttributes textAttributes = colorsScheme.getAttributes(ConsoleViewContentType.ERROR_OUTPUT_KEY);
    if (textAttributes != null && textAttributes.getForegroundColor() != null) {
      return textAttributes.getForegroundColor();
    }
    return UIUtil.getLabelForeground();
  }

  @NotNull
  private String getWarningMessage() {
    if (myStatus == null) {
      return getCommonWarning();
    }
    if (!myStatus.isCoveragePreprocessorSpecifiedInConfig()) {
      return getWarningAboutMissingCoveragePreprocessorInConfigFile();
    }
    if (!myStatus.isCoverageReportFound()) {
      if (myStatus.isKarmaCoveragePackageNeededToBeInstalled()) {
        return getSuggestionAboutCoveragePluginInstallation();
      }
      return getWarningAboutMissingCoveragePluginInConfigFile();
    }
    return "";
  }

  @NotNull
  private static String getCommonWarning() {
    return TITLE + SEE_KARMA_SERVER_TAB;
  }

  @NotNull
  private static Color getCodeBackground() {
    TextAttributesKey[] keys;
    if (UIUtil.isUnderDarcula()) {
      keys = new TextAttributesKey[] { CodeInsightColors.FOLLOWED_HYPERLINK_ATTRIBUTES };
    }
    else {
      keys = new TextAttributesKey[] {
        EditorColors.DELETED_TEXT_ATTRIBUTES,
        CodeInsightColors.FOLLOWED_HYPERLINK_ATTRIBUTES
      };
    }
    EditorColorsScheme colorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
    for (TextAttributesKey key : keys) {
      TextAttributes textAttributes = colorsScheme.getAttributes(key);
      if (textAttributes != null && textAttributes.getBackgroundColor() != null) {
        return textAttributes.getBackgroundColor();
      }
    }
    return UIUtil.getOptionPaneBackground();
  }

  private static String formatHtmlCode(@NotNull String[] lines) {
    Color background = getCodeBackground();
    StringBuilder colorBuf = new StringBuilder();
    UIUtil.appendColor(background, colorBuf);
    return   "<div style='padding-left:10px; padding-top:5px; padding-bottom:5px;'>"
           + "<table cellspacing='0' cellpadding='0' style='border: none;'>"
           + "<tr>"
           + "<td>"
           + "<div style='padding-left:6px;" +
                        " padding-top:2px;" +
                        " padding-bottom:2px;" +
                        " padding-right:6px;" +
                        " background-color:#" + colorBuf.toString() + ";'>"
           + "<pre><code>"
           + StringUtil.join(lines, "\n")
           + "</code></pre>"
           + "</div>"
           + "</td>"
           + "<td></td>"
           + "</tr>"
           + "</table>"
           + "</div>";
  }

  @NotNull
  private static String getWarningAboutMissingCoveragePreprocessorInConfigFile() {
    return TITLE
           + "<div style='padding-top:3px'>Make sure coverage preprocessor is configured like this:</div>"
           + formatHtmlCode(new String[]{
              "module.exports = function (config) {",
              "  config.set({",
              "    ...",
              "    preprocessors: {",
              "      // source files, that you wanna generate coverage for",
              "      // do not include tests or libraries",
              "      // (these files will be instrumented by Istanbul)",
              "      'src/*.js': ['coverage']",
              "    },",
              "    ...",
              "  });",
              "};"
             })
           + "As the preprocessor is configured, run with coverage again.";
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
    return TITLE
           + SEE_KARMA_SERVER_TAB
           + "<div style='padding-top:3px'>It seems that 'karma-coverage' node package isn't installed.</div>"
           + "<div style='padding-top:3px'>To install it execute the following commands:</div>"
           + formatHtmlCode(new String[]{
              "cd " + path,
              "npm install karma-coverage"
             })
           + "As the package is installed, run with coverage again.";
  }

  @NotNull
  private static String getWarningAboutMissingCoveragePluginInConfigFile() {
    return TITLE
           + SEE_KARMA_SERVER_TAB
           + "<div style='padding-top:3px'>It seems that <code>'coverage'</code> reporter isn't available.</div>"
           + "<div style='padding-top:3px; padding-bottom:3px\'>"
           +   "Make sure <code>'karma-coverage'</code> plugin is specified like this:"
           + "</div>"
           + formatHtmlCode(new String[]{
              "module.exports = function (config) {",
              "  config.set({",
              "    ...",
              "    plugins: [..., 'karma-coverage'],",
              "    ...",
              "  });",
              "};"
             })
           + "As the plugin is specified, run with coverage again.";
  }

  @Override
  public JComponent getPreferredFocusableComponent() {
    return null;
  }

  @Override
  public void dispose() {
  }

}
