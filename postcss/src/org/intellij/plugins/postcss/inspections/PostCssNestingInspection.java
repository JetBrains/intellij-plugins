package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.css.CssSelectorList;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.impl.PostCssElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PostCssNestingInspection extends PostCssBaseInspection {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {
      @Override
      public void visitCssSelector(CssSelector selector) {
        if (!PostCssPsiUtil.isInsidePostCss(selector)) return;
        if (PostCssPsiUtil.isInsideNestedRuleset(selector)) {
          PostCssInspectionUtil.completeAllSelectorsWithoutAmpersand(selector, holder);
        }
        else {
          PostCssInspectionUtil.checkNotContainsNestingSelectors(selector, holder);
        }
      }

      @Override
      public void visitCssSelectorList(CssSelectorList selectorList) {
        if (!PostCssPsiUtil.isInsidePostCss(selectorList)) return;
        if (PostCssPsiUtil.isInsideNestedRuleset(selectorList)) {
          PostCssInspectionUtil.addAtRuleNesting(selectorList, holder);
        }
      }
    };
  }
}