package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.css.inspections.fixes.CssAddPrefixQuickFix;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssCustomSelectorAtRule;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.impl.PostCssCustomSelectorAtRuleImpl;
import org.intellij.plugins.postcss.psi.impl.PostCssCustomSelectorImpl;
import org.intellij.plugins.postcss.psi.impl.PostCssElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorInspection extends PostCssBaseInspection {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {

      @Override
      public void visitPostCssCustomSelector(PostCssCustomSelectorImpl postCssCustomSelector) {
        if (!PostCssPsiUtil.isInsidePostCss(postCssCustomSelector)) return;
        if (PsiTreeUtil.hasErrorElements(postCssCustomSelector.getParent())) return;
        String text = postCssCustomSelector.getText();
        if (StringUtil.isEmpty(text)) {
          String description = PostCssBundle.message("annotator.custom.selector.name.expected");
          TextRange textRange = TextRange.from(postCssCustomSelector.getStartOffsetInParent(), 1);
          ProblemDescriptor problemDescriptor = holder.getManager().createProblemDescriptor(
            postCssCustomSelector.getParent(), textRange, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true);
          holder.registerProblem(problemDescriptor);
        }
        else if (text.equals(":--")) {
          holder.registerProblem(postCssCustomSelector, PostCssBundle.message("annotator.custom.selector.name.should.not.be.empty"));
        }
        else if (!StringUtil.startsWith(text, ":--")) {
          LocalQuickFix quickFix =
            new CssAddPrefixQuickFix(":--", PostCssCustomSelectorAtRule.class,
                                     PostCssBundle.message("annotator.add.prefix.to.custom.selector.quickfix.name"));
          String description = PostCssBundle.message("annotator.custom.selector.name.should.start.with");
          TextRange textRange = TextRange.from(postCssCustomSelector.getStartOffsetInParent(), postCssCustomSelector.getTextLength());
          ProblemDescriptor problemDescriptor = holder.getManager().createProblemDescriptor(
            postCssCustomSelector.getParent(), textRange, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, quickFix);
          holder.registerProblem(problemDescriptor);
        }
      }

      @Override
      public void visitPostCssCustomSelectorAtRule(PostCssCustomSelectorAtRuleImpl postCssCustomSelectorAtRule) {
        if (PsiTreeUtil.hasErrorElements(postCssCustomSelectorAtRule)) return;
        CssSelectorList selectorList = postCssCustomSelectorAtRule.getSelectorList();
        if (selectorList == null) return;
        if (selectorList.getText().isEmpty()) {
          String description = PostCssBundle.message("annotator.custom.selector.at.rule.should.contain.selector.list");
          TextRange textRange = TextRange.from(selectorList.getStartOffsetInParent(), 1);
          ProblemDescriptor problemDescriptor = holder.getManager().createProblemDescriptor(
            postCssCustomSelectorAtRule, textRange, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true);
          holder.registerProblem(problemDescriptor);
        }
      }
    };
  }
}