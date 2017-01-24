package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.FormattingMode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.jetbrains.lang.dart.DartLanguage;

public class DartBlockContext {
  private final CodeStyleSettings mySettings;
  private final FormattingMode myMode;
  private final CommonCodeStyleSettings myDartSettings;

  public DartBlockContext(CodeStyleSettings settings, FormattingMode mode) {
    mySettings = settings;
    myMode = mode;
    myDartSettings = settings.getCommonSettings(DartLanguage.INSTANCE);
  }

  public CodeStyleSettings getSettings() {
    return mySettings;
  }

  public CommonCodeStyleSettings getDartSettings() {
    return myDartSettings;
  }

  public FormattingMode getMode() {
    return myMode;
  }
}
