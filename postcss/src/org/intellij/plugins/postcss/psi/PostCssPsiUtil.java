package org.intellij.plugins.postcss.psi;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssElement;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlToken;
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

  public static boolean isInsidePostCss(@Nullable CssElement element) {
    return CssPsiUtil.getStylesheetLanguage(element) == PostCssLanguage.INSTANCE;
  }

  public static boolean isInsideNestedRuleset(@Nullable CssElement element) {
    return PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(element, CssRuleset.class), CssRuleset.class) != null;
  }

  public static boolean containsAmpersand(@Nullable CssSelector selector) {
    return selector != null && Arrays.stream(selector.getSimpleSelectors()).anyMatch(PostCssPsiUtil::isAmpersand);
  }

  public static boolean startsWithAmpersand(@NotNull CssSelector selector) {
    return isAmpersand(ArrayUtil.getFirstElement(selector.getSimpleSelectors()));
  }

  public static boolean isInsideNest(@Nullable PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PostCssNest.class) != null;
  }

  @Contract("null -> false")
  public static boolean isAmpersand(@Nullable PsiElement element) {
    if (element == null) return false;
    PsiElement firstChild = element.getFirstChild();
    return firstChild instanceof XmlToken && ((XmlToken)firstChild).getTokenType() == PostCssTokenTypes.AMPERSAND;
  }

  @Contract("null -> false")
  public static boolean isNestSym(@Nullable PsiElement element) {
    if (element == null) return false;
    PsiElement firstChild = element.getFirstChild();
    return firstChild instanceof XmlToken && ((XmlToken)firstChild).getTokenType() == PostCssTokenTypes.POST_CSS_NEST_SYM;
  }
}