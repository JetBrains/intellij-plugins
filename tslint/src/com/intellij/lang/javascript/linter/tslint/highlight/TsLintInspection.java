package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.tslint.service.TslintLanguageServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public final class TsLintInspection extends JSLinterInspection {

  public boolean useSeverityFromConfigFile = true;

  @NotNull
  @Override
  protected TsLintExternalAnnotator getExternalAnnotatorForBatchInspection() {
    return TsLintExternalAnnotator.getInstanceForBatchInspection();
  }

  @NotNull
  @Override
  protected HighlightSeverity chooseSeverity(@NotNull HighlightSeverity fromError, @NotNull HighlightSeverity inspectionSeverity) {
    return useSeverityFromConfigFile ? fromError : inspectionSeverity;
  }

  @Override
  public JComponent createOptionsPanel() {
    return createOptionPanelForConfigFileOption("useSeverityFromConfigFile");
  }

  @Override
  protected void ensureServiceStopped(@NotNull Project project) {
    TslintLanguageServiceManager.getInstance(project).terminateServices();
  }

  @Override
  public SuppressQuickFix @NotNull [] getBatchSuppressActions(@Nullable PsiElement element) {
    return SuppressQuickFix.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getGroupDisplayName() {
    return JavaScriptBundle.message("typescript.inspection.group.name");
  }

  @NotNull
  @Override
  protected List<String> getSettingsPath() {
    return ContainerUtil.newArrayList(
      JavaScriptBundle.message("typescript.compiler.configurable.name"),
      getDisplayName()
    );
  }
}
