package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.CustomSuppressableInspectionTool;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.inspections.suppress.GherkinSuppressionUtil;

/**
 * @author Roman.Chernyatchik
 */
public abstract class GherkinInspection extends LocalInspectionTool implements CustomSuppressableInspectionTool {
  @NotNull
  public String getGroupDisplayName() {
    return CucumberBundle.message("cucumber.inspection.group.name");
  }

  public SuppressIntentionAction[] getSuppressActions(@Nullable final PsiElement element) {
    return GherkinSuppressionUtil.getDefaultSuppressActions(element, getShortName());
  }

  public boolean isSuppressedFor(@NotNull final PsiElement element) {
    return GherkinSuppressionUtil.isSuppressedFor(element, getID());
  }
}