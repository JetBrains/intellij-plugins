package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;

/**
 * Provides a setup and tear down for tests to use to set up the app test fixture
 * with a standard set of formatter settings
 */
public class FormatterTestSettings {
  private final Project myProject;
  private boolean myPrevFormatSetting;
  private int myPrevIndentSize;

  public FormatterTestSettings(Project project) {
    myProject = project;
  }

  public void setUp() {
    myPrevFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(true);

    myPrevIndentSize = CodeStyleSettingsManager.getSettings(myProject).getIndentOptions(StdFileTypes.HTML).INDENT_SIZE;
    CodeStyleSettingsManager.getSettings(myProject).getIndentOptions(StdFileTypes.HTML).INDENT_SIZE = 4;
  }

  public void tearDown() {
    HbConfig.setFormattingEnabled(myPrevFormatSetting);
    CodeStyleSettingsManager.getSettings(myProject).getIndentOptions(StdFileTypes.HTML).INDENT_SIZE = myPrevIndentSize;
  }
}
