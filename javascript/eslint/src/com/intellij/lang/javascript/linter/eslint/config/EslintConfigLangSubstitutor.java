package com.intellij.lang.javascript.linter.eslint.config;

import com.intellij.lang.javascript.linter.JSLinterConfigLangSubstitutor;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;

public class EslintConfigLangSubstitutor extends JSLinterConfigLangSubstitutor {
  public EslintConfigLangSubstitutor() {
    super(EslintUtil.DEFAULT_CONFIG_PREFIX);
  }
}
