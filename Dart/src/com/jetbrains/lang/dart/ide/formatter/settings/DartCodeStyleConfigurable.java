package com.jetbrains.lang.dart.ide.formatter.settings;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class DartCodeStyleConfigurable extends CodeStyleAbstractConfigurable {
  public DartCodeStyleConfigurable(@NotNull CodeStyleSettings settings, CodeStyleSettings cloneSettings) {
    super(settings, cloneSettings, "Dart");
  }

  @Override
  protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
    return new DartCodeStyleMainPanel(getCurrentSettings(), settings);
  }

  @Override
  public String getHelpTopic() {
    return "reference.settingsdialog.codestyle.dart";
  }
}
