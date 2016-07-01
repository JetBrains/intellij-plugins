package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

public class PostCssDeleteAtRuleNestQuickFix extends LocalQuickFixBase {
  public PostCssDeleteAtRuleNestQuickFix() {
    super(PostCssBundle.message("annotator.delete.at.rule.nest.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getStartElement();
    if (PostCssPsiUtil.isNestSym(element)){
      element.delete();
    }
  }
}