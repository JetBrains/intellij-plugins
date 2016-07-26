package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssSelectorList;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.actions.PostCssAddPrefixToCustomSelectorQuickFix;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.impl.PostCssCustomSelectorAtRuleImpl;
import org.intellij.plugins.postcss.psi.impl.PostCssCustomSelectorImpl;
import org.intellij.plugins.postcss.psi.impl.PostCssElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorInspection extends PostCssBaseInspection {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {

      @Override
      public void visitPostCssCustomSelector(PostCssCustomSelectorImpl postCssCustomSelector) {
        if (!PostCssPsiUtil.isInsidePostCss(postCssCustomSelector)) return;
        String text = postCssCustomSelector.getText();
        if (StringUtil.isEmpty(text)) {
          String description = PostCssBundle.message("annotator.custom.selector.name.expected");
          TextRange textRange =
            new TextRange(postCssCustomSelector.getStartOffsetInParent(), postCssCustomSelector.getStartOffsetInParent() + 1);
          ProblemDescriptor problemDescriptor = holder.getManager().createProblemDescriptor(
            postCssCustomSelector.getParent(), textRange, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true);
          holder.registerProblem(problemDescriptor);
        }
        else if (text.equals(":--")) {
          holder.registerProblem(postCssCustomSelector, PostCssBundle.message("annotator.custom.selector.name.should.not.be.empty"));
        }
        else if (!StringUtil.startsWith(text, ":--")) {
          holder.registerProblem(postCssCustomSelector, PostCssBundle.message("annotator.custom.selector.name.should.start.with"),
                                 new PostCssAddPrefixToCustomSelectorQuickFix());
        }
      }

      @Override
      public void visitPostCssCustomSelectorAtRule(PostCssCustomSelectorAtRuleImpl postCssCustomSelectorAtRule) {
        CssSelectorList selectorList = postCssCustomSelectorAtRule.getSelectorList();
        if (selectorList == null) return;
        if (selectorList.getText().isEmpty()) {
          String description = PostCssBundle.message("annotator.custom.selector.at.rule.should.contain.selector.list");
          TextRange textRange = new TextRange(selectorList.getStartOffsetInParent(), postCssCustomSelectorAtRule.getTextLength());
          ProblemDescriptor problemDescriptor = holder.getManager().createProblemDescriptor(
            postCssCustomSelectorAtRule, textRange, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true);
          holder.registerProblem(problemDescriptor);
        }
      }
    };
  }
}