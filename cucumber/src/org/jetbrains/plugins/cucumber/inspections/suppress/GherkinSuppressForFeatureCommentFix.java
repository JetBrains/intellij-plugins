package org.jetbrains.plugins.cucumber.inspections.suppress;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.actions.AbstractSuppressByNoInspectionCommentFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;

/**
 * @author Roman.Chernyatchik
 * @date Aug 13, 2009
 */
public class GherkinSuppressForFeatureCommentFix extends AbstractSuppressByNoInspectionCommentFix {
  GherkinSuppressForFeatureCommentFix(@NotNull final String actionShortName) {
    super(HighlightDisplayKey.find(actionShortName).getID(), false);
  }

  @NotNull
  @Override
  public String getText() {
    return CucumberBundle.message("cucumber.inspection.suppress.feature");
  }

  @Override
  protected PsiElement getContainer(PsiElement context) {
    // step
    return PsiTreeUtil.getNonStrictParentOfType(context, GherkinFeature.class);
  }
}
