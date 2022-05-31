package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class ActionScriptCodeStyleSettingsConfigurable extends CodeStyleAbstractConfigurable {

  public ActionScriptCodeStyleSettingsConfigurable(@NotNull CodeStyleSettings settings,
                                                   CodeStyleSettings cloneSettings) {
    super(settings, cloneSettings, "ActionScript");
  }

  @Override
  protected @NotNull CodeStyleAbstractPanel createPanel(@NotNull CodeStyleSettings settings) {
    return new ActionScriptCodeStyleMainPanel(getCurrentSettings(), settings);
  }

  @Override
  public String getHelpTopic() {
    return "reference.settingsdialog.codestyle.actionscript";
  }
}
