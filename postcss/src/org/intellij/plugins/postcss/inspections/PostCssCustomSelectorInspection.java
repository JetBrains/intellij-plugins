package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssSelectorList;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.actions.PostCssAddPrefixToCustomSelectorQuickFix;
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
        String text = postCssCustomSelector.getText();
        if (text.equals(":--")) {
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
        if (selectorList == null || selectorList.getText().isEmpty()) {
          holder.registerProblem(postCssCustomSelectorAtRule,
                                 PostCssBundle.message("annotator.custom.selector.at.rule.should.contain.selector.list"));
        }
      }
    };
  }
}