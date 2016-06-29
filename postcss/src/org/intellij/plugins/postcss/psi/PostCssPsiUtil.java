package org.intellij.plugins.postcss.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Arrays;

public class PostCssPsiUtil {
  private PostCssPsiUtil() {
  }

  public static boolean isInsideNestedRuleset(CssElement element) {
    if (element == null) return false;
    CssRuleset ruleset = PsiTreeUtil.getParentOfType(element, CssRuleset.class);
    return isNestedRuleset(ruleset);
  }

  public static boolean isNestedRuleset(CssRuleset ruleset) {
    if (ruleset == null) return false;
    CssRuleset parentRuleset = PsiTreeUtil.getParentOfType(ruleset, CssRuleset.class);
    return parentRuleset != null;
  }

  public static boolean containsAmpersand(CssSelector selector) {
    return Arrays.stream(selector.getSimpleSelectors()).anyMatch(PostCssPsiUtil::isAmpersand);
  }

  public static boolean startsWithAmpersand(CssSelector selector) {
    if (selector.getSimpleSelectors().length == 0) return false;
    return isAmpersand(selector.getSimpleSelectors()[0]);
  }

  public static boolean isStartWithNest(CssSelectorList list) {
    CssSelector[] selectors = list.getSelectors();
    if (selectors.length == 0) return false;
    CssSimpleSelector[] simpleSelectors = selectors[0].getSimpleSelectors();
    if (simpleSelectors.length == 0) return false;
    return isNest(simpleSelectors[0]);
  }

  public static boolean isAmpersand(CssSimpleSelector selector) {
    PsiElement firstChild = selector.getFirstChild();
    return firstChild != null && firstChild instanceof PostCssDirectNest;
  }

  public static boolean isNest(CssSimpleSelector selector) {
    PsiElement firstChild = selector.getFirstChild();
    return firstChild != null && firstChild instanceof PostCssNestSym;
  }
}