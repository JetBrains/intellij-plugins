package org.intellij.plugins.postcss.psi;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssLanguage;

import java.util.Arrays;

public class PostCssPsiUtil {
  private PostCssPsiUtil() {
  }

  public static boolean isInsidePostCss(CssElement element) {
    return CssPsiUtil.getStylesheetLanguage(element) == PostCssLanguage.INSTANCE;
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

  public static boolean isInsideNest(CssSelector selector) {
    CssSelectorList selectorList = PsiTreeUtil.getParentOfType(selector, CssSelectorList.class);
    return isInsideNest(selectorList);
  }

  public static boolean isInsideNest(CssSelectorList list) {
    return PsiTreeUtil.getParentOfType(list, PostCssNest.class, true) != null;
  }

  public static boolean isAmpersand(CssSimpleSelector selector) {
    PsiElement firstChild = selector.getFirstChild();
    return firstChild != null && firstChild instanceof PostCssDirectNest;
  }

  public static boolean isNestSym(CssSimpleSelector selector) {
    PsiElement firstChild = selector.getFirstChild();
    return firstChild != null && firstChild instanceof PostCssNestSym;
  }
}