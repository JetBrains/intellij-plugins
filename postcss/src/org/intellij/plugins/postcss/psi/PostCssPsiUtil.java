package org.intellij.plugins.postcss.psi;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PostCssPsiUtil {
  private PostCssPsiUtil() {
  }

  @Contract("null -> false")
  public static boolean isInsidePostCss(@Nullable PsiElement element) {
    return CssPsiUtil.getStylesheetLanguage(element) == PostCssLanguage.INSTANCE;
  }

  @Contract("null -> true")
  public static boolean isEmptyElement(@Nullable PsiElement element) {
    return element == null || element.getTextLength() == 0;
  }

  @Contract("null -> false")
  public static boolean isInsideNestedRuleset(@Nullable PsiElement element) {
    CssRuleset parent = PsiTreeUtil.getParentOfType(element, CssRuleset.class);
    return PsiTreeUtil.getParentOfType(parent, CssRuleset.class) != null;
  }

  @Contract("null -> false")
  public static boolean isInsideCustomSelector(@Nullable PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PostCssCustomSelectorAtRule.class) != null;
  }

  @Contract("null -> false")
  public static boolean containsAmpersand(@Nullable CssSelector selector) {
    return selector != null && Arrays.stream(selector.getSimpleSelectors()).anyMatch(PostCssPsiUtil::isAmpersand);
  }

  @Contract("null -> false")
  public static boolean startsWithAmpersand(@NotNull CssSelector selector) {
    return isAmpersand(ArrayUtil.getFirstElement(selector.getSimpleSelectors()));
  }

  @Contract("null -> false")
  public static boolean isInsideNest(@Nullable PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PostCssNest.class, CssRuleset.class, CssAtRule.class) instanceof PostCssNest;
  }

  @Contract("null -> false")
  public static boolean isAmpersand(@Nullable PsiElement element) {
    if (element == null) return false;
    PsiElement firstChild = element.getFirstChild();
    return firstChild instanceof LeafElement && ((LeafElement)firstChild).getElementType() == PostCssTokenTypes.AMPERSAND;
  }

}