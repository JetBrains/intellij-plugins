package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.css.CssSimpleSelector;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.actions.PostCssAddAmpersandToSelectorQuickFix;
import org.intellij.plugins.postcss.actions.PostCssAddAtRuleNestToSelectorQuickFix;
import org.intellij.plugins.postcss.actions.PostCssDeleteAmpersandQuickFix;
import org.intellij.plugins.postcss.actions.PostCssDeleteAtRuleNestQuickFix;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.impl.PostCssElementVisitor;
import org.intellij.plugins.postcss.psi.impl.PostCssNestImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PostCssNestingInspection extends PostCssBaseInspection {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {
      @Override
      public void visitCssSelector(CssSelector selector) {
        if (PostCssPsiUtil.isEmptyElement(selector) || !PostCssPsiUtil.isInsidePostCss(selector)) return;
        if (PostCssPsiUtil.isInsideNestedRuleset(selector)) {
          annotateNestedSelectorsWithoutAmpersand(selector, holder);
        }
        else {
          annotateTopLevelSelectorsWithNestingSigns(selector, holder);
        }
      }

      @Override
      public void visitCssSelectorList(CssSelectorList selectorList) {
        if (PostCssPsiUtil.isEmptyElement(selectorList) || !PostCssPsiUtil.isInsidePostCss(selectorList)) return;
        if (PostCssPsiUtil.isInsideNestedRuleset(selectorList)) {
          annotateNestedSelectorsWithoutNest(selectorList, holder);
        }
      }

      @Override
      public void visitPostCssNest(PostCssNestImpl postCssNest) {
        if (PostCssPsiUtil.isEmptyElement(postCssNest) || !PostCssPsiUtil.isInsidePostCss(postCssNest)) return;
        CssSelectorList selectorList = postCssNest.getSelectorList();
        if (PostCssPsiUtil.isInsideNestedRuleset(selectorList) && PostCssPsiUtil.isEmptyElement(selectorList)) {
          holder
            .registerProblem(postCssNest.getFirstChild(), PostCssBundle.message("annotator.nested.selector.doesnt.have.ampersand.error"));
        }
      }
    };
  }

  private static void annotateNestedSelectorsWithoutAmpersand(CssSelector selector, ProblemsHolder holder) {
    if (PostCssPsiUtil.isInsideNest(selector)) {
      if (!PostCssPsiUtil.containsAmpersand(selector)) {
        holder.registerProblem(selector, PostCssBundle.message("annotator.nested.selector.doesnt.have.ampersand.error"));
      }
    }
    else if (!PostCssPsiUtil.containsAmpersand(selector)) {
      holder.registerProblem(selector, PostCssBundle.message("annotator.nested.selector.doesnt.starts.with.ampersand.error"),
                             new PostCssAddAmpersandToSelectorQuickFix());
    }
  }

  private static void annotateTopLevelSelectorsWithNestingSigns(CssSelector selector, ProblemsHolder holder) {
    CssSimpleSelector[] directNests =
      Arrays.stream(selector.getSimpleSelectors()).filter(PostCssPsiUtil::isAmpersand).toArray(CssSimpleSelector[]::new);
    if (directNests != null) {
      for (CssSimpleSelector directNest : directNests) {
        holder.registerProblem(directNest, PostCssBundle
          .message("annotator.normal.selector.contains.direct.nesting.selector"), new PostCssDeleteAmpersandQuickFix());
      }
    }
    CssSimpleSelector[] nests =
      Arrays.stream(selector.getSimpleSelectors()).filter(PostCssPsiUtil::isNestSym).toArray(CssSimpleSelector[]::new);
    if (nests != null) {
      for (CssSimpleSelector nest : nests) {
        holder.registerProblem(nest, PostCssBundle
          .message("annotator.normal.selector.contains.nest"), new PostCssDeleteAtRuleNestQuickFix());
      }
    }
  }

  private static void annotateNestedSelectorsWithoutNest(CssSelectorList list, ProblemsHolder holder) {
    if (PostCssPsiUtil.isInsideNest(list)) return;
    boolean everySelectorHasAmpersand = Arrays.stream(list.getSelectors()).allMatch(PostCssPsiUtil::containsAmpersand);
    boolean everySelectorStartsWithAmpersand = Arrays.stream(list.getSelectors()).allMatch(PostCssPsiUtil::startsWithAmpersand);
    if (everySelectorHasAmpersand && !everySelectorStartsWithAmpersand) {
      holder.registerProblem(list, PostCssBundle.message("annotator.nested.selector.list.doesnt.have.nest.at.rule.error"),
                             new PostCssAddAtRuleNestToSelectorQuickFix());
    }
  }
}