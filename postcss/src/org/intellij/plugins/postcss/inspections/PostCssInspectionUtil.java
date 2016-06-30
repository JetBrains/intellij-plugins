package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.css.CssSimpleSelector;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;

import java.util.Arrays;

public class PostCssInspectionUtil {
  private PostCssInspectionUtil() {
  }

  public static void completeAllSelectorsWithoutAmpersand(CssSelector selector, ProblemsHolder holder) {
    if (PostCssPsiUtil.isInsideNest(selector)) {
      if (!PostCssPsiUtil.containsAmpersand(selector)) {
        holder.registerProblem(selector, PostCssBundle.message("annotator.nested.selector.doesnt.have.ampersand.error"));
      }
    }
    else if (!PostCssPsiUtil.containsAmpersand(selector)) {
      holder.registerProblem(selector, PostCssBundle.message("annotator.nested.selector.doesnt.starts.with.ampersand.error"));
    }
  }

  public static void checkNotContainsNestingSelectors(CssSelector selector, ProblemsHolder holder) {
    CssSimpleSelector[] directNests =
      Arrays.stream(selector.getSimpleSelectors()).filter(PostCssPsiUtil::isAmpersand).toArray(CssSimpleSelector[]::new);
    if (directNests != null) {
      for (CssSimpleSelector directNest : directNests) {
        holder.registerProblem(directNest, PostCssBundle
          .message("annotator.normal.selector.contains.direct.nesting.selector"));
      }
    }
    CssSimpleSelector[] nests =
      Arrays.stream(selector.getSimpleSelectors()).filter(PostCssPsiUtil::isNestSym).toArray(CssSimpleSelector[]::new);
    if (nests != null) {
      for (CssSimpleSelector nest : nests) {
        holder.registerProblem(nest, PostCssBundle
          .message("annotator.normal.selector.contains.nest"));
      }
    }
  }

  public static void addAtRuleNesting(CssSelectorList list, ProblemsHolder holder) {
    if (PostCssPsiUtil.isInsideNest(list)) return;
    boolean everySelectorHasAmpersand = Arrays.stream(list.getSelectors()).allMatch(PostCssPsiUtil::containsAmpersand);
    boolean everySelectorStartsWithAmpersand = Arrays.stream(list.getSelectors()).allMatch(PostCssPsiUtil::startsWithAmpersand);
    if (everySelectorHasAmpersand && !everySelectorStartsWithAmpersand) {
      holder.registerProblem(list, PostCssBundle.message("annotator.nested.selector.list.doesnt.have.nest.at.rule.error"));
    }
  }
}