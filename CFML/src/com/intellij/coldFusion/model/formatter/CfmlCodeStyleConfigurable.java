package com.intellij.coldFusion.model.formatter;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlCodeStyleConfigurable extends CodeStyleAbstractConfigurable {
  public CfmlCodeStyleConfigurable(@NotNull CodeStyleSettings settings, CodeStyleSettings cloneSettings) {
    super(settings, cloneSettings, "CFML");
  }

  @Override
  protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
    return new CfmlCodeStyleMainPanel(getCurrentSettings(), settings);
  }

  @Override
  public String getHelpTopic() {
    return "reference.settingsdialog.codestyle.cfml";
  }
}

