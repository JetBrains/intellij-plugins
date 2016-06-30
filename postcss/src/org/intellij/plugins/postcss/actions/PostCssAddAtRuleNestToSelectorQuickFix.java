package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssElement;
import com.intellij.psi.css.CssRuleset;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssElementGenerator;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.impl.PostCssNestImpl;
import org.jetbrains.annotations.NotNull;

public class PostCssAddAtRuleNestToSelectorQuickFix extends LocalQuickFixBase {
  public PostCssAddAtRuleNestToSelectorQuickFix() {
    super(PostCssBundle.message("annotator.add.at.rule.nest.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (startElement instanceof CssElement) {
      CssRuleset ruleset = PostCssPsiUtil.getParentRuleset((CssElement)startElement);
      if (ruleset == null) return;
      if (ruleset.getText() == null) return;
      PostCssNestImpl nest = PostCssElementGenerator.createAtRuleNest(project, "@nest " + ruleset.getText());
      if (nest == null) return;
      ruleset.replace(nest);
    }
  }
}