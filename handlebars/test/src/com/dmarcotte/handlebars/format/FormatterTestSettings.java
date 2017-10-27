package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings;

/**
 * Provides a setup and tear down for tests to use to set up the app test fixture
 * with a standard set of formatter settings
 */
public class FormatterTestSettings {
  private final CodeStyleSettings mySettings;
  private boolean myPrevFormatSetting;
  private int myPrevIndentSize;
  private String myPrevDoNotIndentSetting;

  public FormatterTestSettings(CodeStyleSettings codeStyleSettings) {
    mySettings = codeStyleSettings;
  }

  public void setUp() {
    myPrevFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(true);

    myPrevIndentSize = mySettings.getIndentOptions(StdFileTypes.HTML).INDENT_SIZE;
    mySettings.getIndentOptions(StdFileTypes.HTML).INDENT_SIZE = 4;

    myPrevDoNotIndentSetting = mySettings.getCustomSettings(HtmlCodeStyleSettings.class).HTML_DO_NOT_INDENT_CHILDREN_OF;
    mySettings.getCustomSettings(HtmlCodeStyleSettings.class).HTML_DO_NOT_INDENT_CHILDREN_OF = "";
  }

  public void tearDown() {
    HbConfig.setFormattingEnabled(myPrevFormatSetting);
    mySettings.getIndentOptions(StdFileTypes.HTML).INDENT_SIZE = myPrevIndentSize;
    mySettings.getCustomSettings(HtmlCodeStyleSettings.class).HTML_DO_NOT_INDENT_CHILDREN_OF = myPrevDoNotIndentSetting;
  }
}
