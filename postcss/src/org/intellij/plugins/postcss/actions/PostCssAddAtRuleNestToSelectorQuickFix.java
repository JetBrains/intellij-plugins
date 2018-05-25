package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssElementGenerator;
import org.jetbrains.annotations.NotNull;

public class PostCssAddAtRuleNestToSelectorQuickFix extends LocalQuickFixBase {
  public PostCssAddAtRuleNestToSelectorQuickFix() {
    super(PostCssBundle.message("annotator.add.at.rule.nest.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    CssRuleset ruleset = PsiTreeUtil.getParentOfType(descriptor.getStartElement(), CssRuleset.class);
    if (ruleset != null && ruleset.getText() != null) {
      ruleset.replace(PostCssElementGenerator.createAtRuleNest(project, ruleset.getText()));
    }
  }
}