package com.intellij.coldFusion.model.formatter;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlCodeStyleSettings extends CustomCodeStyleSettings {

  public boolean INDENT_CODE_IN_CFML_TAGS = true;
  public boolean ALIGN_KEY_VALUE_PAIRS = false;
  public boolean ALIGN_CFMLDOC_PARAM_NAMES = false;
  public boolean ALIGN_CFMLDOC_COMMENTS = false;
  public boolean ALIGN_ASSIGNMENTS = false;
  public boolean CONCAT_SPACES = true;

  public boolean CFMLDOC_BLANK_LINE_BEFORE_TAGS = false;
  public boolean CFMLDOC_BLANK_LINES_AROUND_PARAMETERS = false;

  public CfmlCodeStyleSettings(CodeStyleSettings container) {
    super("CfmlCodeStyleSettings", container);
  }
}
