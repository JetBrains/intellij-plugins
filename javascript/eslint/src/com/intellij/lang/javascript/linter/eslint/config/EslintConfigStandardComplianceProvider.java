package com.intellij.lang.javascript.linter.eslint.config;

import com.intellij.json.codeinsight.JsonStandardComplianceProvider;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.psi.PsiComment;
import org.jetbrains.annotations.NotNull;

public class EslintConfigStandardComplianceProvider extends JsonStandardComplianceProvider {
  @Override
  public boolean isCommentAllowed(@NotNull PsiComment comment) {
    return EslintUtil.isFlatOrLegacyConfigFile(comment);
  }
}
