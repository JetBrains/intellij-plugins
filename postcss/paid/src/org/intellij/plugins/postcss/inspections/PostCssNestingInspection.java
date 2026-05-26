package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.css.CssSelectorList;
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
  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {
      @Override
      public void visitCssSelector(CssSelector selector) {
        if (PostCssPsiUtil.isEmptyElement(selector) || !PostCssPsiUtil.isInsidePostCss(selector) ||
            PostCssPsiUtil.isInsideCustomSelector(selector)) {
          return;
        }
        if (PostCssPsiUtil.isInsideNestedRuleset(selector)) {
          annotateNestedSelectorsWithoutAmpersand(selector, holder);
        }
      }

      @Override
      public void visitCssSelectorList(CssSelectorList selectorList) {
        if (PostCssPsiUtil.isEmptyElement(selectorList) || !PostCssPsiUtil.isInsidePostCss(selectorList) ||
            PostCssPsiUtil.isInsideCustomSelector(selectorList)) {
          return;
        }
        if (PostCssPsiUtil.isInsideNestedRuleset(selectorList)) {
          annotateNestedSelectorsWithoutNest(selectorList, holder);
        }
        else {
          annotateTopLevelSelectorsWithNestingSigns(selectorList, holder);
        }
      }

      @Override
      public void visitPostCssNest(PostCssNestImpl postCssNest) {
        if (PostCssPsiUtil.isEmptyElement(postCssNest) || !PostCssPsiUtil.isInsidePostCss(postCssNest) ||
            PostCssPsiUtil.isInsideCustomSelector(postCssNest)) {
          return;
        }
        CssSelectorList selectorList = postCssNest.getSelectorList();
        if (PostCssPsiUtil.isInsideNestedRuleset(selectorList)) {
          if (PostCssPsiUtil.isEmptyElement(selectorList)) {
            holder
              .registerProblem(postCssNest.getFirstChild(), PostCssBundle.message("annotator.nested.selector.doesnt.have.ampersand.error"));
          }
        }
        else {
          annotateTopLevelSelectorsWithNest(postCssNest, holder);
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

  private static void annotateTopLevelSelectorsWithNestingSigns(CssSelectorList selectorList, ProblemsHolder holder) {
    for (TextRange range : PostCssPsiUtil.findAllAmpersands(selectorList)) {
      ProblemDescriptor problemDescriptor = holder.getManager().createProblemDescriptor(
        selectorList, range, PostCssBundle.message("annotator.normal.selector.contains.direct.nesting.selector"),
        ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, new PostCssDeleteAmpersandQuickFix());
      holder.registerProblem(problemDescriptor);
    }
  }

  private static void annotateTopLevelSelectorsWithNest(PostCssNestImpl postCssNest, ProblemsHolder holder) {
    holder.registerProblem(postCssNest.getFirstChild(), PostCssBundle
      .message("annotator.normal.selector.contains.nest"), new PostCssDeleteAtRuleNestQuickFix());
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