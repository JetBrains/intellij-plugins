package org.jetbrains.plugins.cucumber.inspections.suppress;

import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

public class GherkinSuppressForStepCommentFix extends AbstractBatchSuppressByNoInspectionCommentFix {
  GherkinSuppressForStepCommentFix(@NotNull final String toolId) {
    super(toolId, false);
  }

  @NotNull
  @Override
  public String getText() {
    return CucumberBundle.message("cucumber.inspection.suppress.step");
  }

  @Override
  public PsiElement getContainer(PsiElement context) {
    // step
    return PsiTreeUtil.getNonStrictParentOfType(context, GherkinStep.class);
  }
}

