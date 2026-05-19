package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.JSLinterExternalAnnotator;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.openapi.options.OptionsBundle;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StandardJSInspection extends JSLinterInspection {
  @Override
  protected @NotNull JSLinterExternalAnnotator getExternalAnnotatorForBatchInspection() {
    return new StandardJSExternalAnnotator(false);
  }

  @Override
  protected void ensureServiceStopped(@NotNull Project project) {
    StandardJSLanguageServiceManager.getInstance(project).terminateServices();
  }

  @Override
  public SuppressQuickFix @NotNull [] getBatchSuppressActions(@Nullable PsiElement element) {
    return SuppressQuickFix.EMPTY_ARRAY;
  }

  @Override
  protected @NotNull List<String> getSettingsPath() {
    return List.of(
      OptionsBundle.message("configurable.group.language.settings.display.name"),
      EslintBundle.message("settings.javascript.linters.eslint.configurable.name"));
  }
}
