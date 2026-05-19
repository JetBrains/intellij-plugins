package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public interface EslintSettingsConverter {

  boolean inSync(@NotNull CodeStyleSettings settings);

  void apply(@NotNull CodeStyleSettings settings);

  EslintSettingsConverter MISCONFIGURATION = new EslintNoOpSettingsConverter(EslintRuleMapper.RuleState.misconfiguration);
  EslintSettingsConverter SKIPPED = new EslintNoOpSettingsConverter(EslintRuleMapper.RuleState.skipped);
}

class EslintNoOpSettingsConverter implements EslintSettingsConverter {

  private final @NotNull EslintRuleMapper.RuleState myState;

  EslintNoOpSettingsConverter(@NotNull EslintRuleMapper.RuleState state) {
    this.myState = state;
  }

  @Override
  public boolean inSync(@NotNull CodeStyleSettings settings) {
    return true;
  }

  @Override
  public void apply(@NotNull CodeStyleSettings settings) {
  }

  @Override
  public String toString() {
    return "EslintNoOpSettingsConverter (" + myState + ')';
  }
}
