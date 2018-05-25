package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssElementFactory;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NotNull;

public class PostCssDeleteAtRuleNestQuickFix extends LocalQuickFixBase {
  public PostCssDeleteAtRuleNestQuickFix() {
    super(PostCssBundle.message("annotator.delete.at.rule.nest.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getStartElement();
    if (element.getNode().getElementType() == PostCssTokenTypes.POST_CSS_NEST_SYM) {
      PsiElement parent = element.getParent();
      String text = StringUtil.trimStart(parent.getText(), "@nest");
      parent
        .replace(CssElementFactory.getInstance(element.getProject()).createRuleset(text, PostCssLanguage.INSTANCE));
    }
  }
}