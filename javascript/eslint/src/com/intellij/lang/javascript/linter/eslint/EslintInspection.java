package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.codeInspection.options.OptPane;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EslintInspection extends JSLinterInspection {
  public static final String SHORT_NAME = calcShortNameFromClass(EslintInspection.class);
  public boolean useSeverityFromConfigFile = true;

  @Override
  protected @NotNull EslintExternalAnnotator getExternalAnnotatorForBatchInspection() {
    return EslintExternalAnnotator.getInstanceForBatchInspection();
  }

  @Override
  protected @NotNull HighlightSeverity chooseSeverity(@NotNull HighlightSeverity fromError, @NotNull HighlightSeverity inspectionSeverity) {
    return useSeverityFromConfigFile ? fromError : inspectionSeverity;
  }

  @Override
  public @NotNull OptPane getOptionsPane() {
    return getOptionsPaneForConfigFileOption("useSeverityFromConfigFile");
  }

  @Override
  protected void ensureServiceStopped(final @NotNull Project project) {
    EslintLanguageServiceManager.getInstance(project).terminateServices();
  }

  @Override
  public SuppressQuickFix @NotNull [] getBatchSuppressActions(@Nullable PsiElement element) {
    return SuppressQuickFix.EMPTY_ARRAY;
  }
}
