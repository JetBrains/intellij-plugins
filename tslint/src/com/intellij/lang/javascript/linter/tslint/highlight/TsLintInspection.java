// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.codeInspection.options.OptPane;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.tslint.service.TslintLanguageServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TsLintInspection extends JSLinterInspection {

  public boolean useSeverityFromConfigFile = true;

  @Override
  protected @NotNull TsLintExternalAnnotator getExternalAnnotatorForBatchInspection() {
    return TsLintExternalAnnotator.getInstanceForBatchInspection();
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
  protected void ensureServiceStopped(@NotNull Project project) {
    TslintLanguageServiceManager.getInstance(project).terminateServices();
  }

  @Override
  public SuppressQuickFix @NotNull [] getBatchSuppressActions(@Nullable PsiElement element) {
    return SuppressQuickFix.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getGroupDisplayName() {
    return JavaScriptBundle.message("typescript.inspection.group.name");
  }

  @Override
  protected @NotNull List<String> getSettingsPath() {
    return List.of(
      JavaScriptBundle.message("typescript.compiler.configurable.name"),
      getDisplayName()
    );
  }
}
