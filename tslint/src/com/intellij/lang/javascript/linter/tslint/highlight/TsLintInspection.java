package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public final class TsLintInspection extends JSLinterInspection {
  @NotNull
  @Override
  protected TsLintExternalAnnotator getExternalAnnotatorForBatchInspection() {
    return TsLintExternalAnnotator.getInstanceForBatchInspection();
  }

  public static HighlightDisplayKey getHighlightDisplayKey() {
    return JSLinterInspection.getHighlightDisplayKeyByClass(TsLintInspection.class);
  }

  @Override
  protected void ensureServiceStopped(@NotNull Project project) {
    TsLintLanguageService.getService(project).terminateStartedProcess(false);
  }

  @NotNull
  @Override
  public SuppressQuickFix[] getBatchSuppressActions(@Nullable PsiElement element) {
    return SuppressQuickFix.EMPTY_ARRAY;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return TsLintBundle.message("tslint.framework.title");
  }

  @NotNull
  @Override
  public String getGroupDisplayName() {
    return JSBundle.message("typescript.inspection.group.name");
  }

  @NotNull
  @Override
  protected List<String> getSettingsPath() {
    return ContainerUtil.newArrayList(
      JSBundle.message("typescript.compiler.configurable.name"),
      getDisplayName()
    );
  }
}
