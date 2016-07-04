package org.intellij.plugins.postcss.psi;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssElement;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.css.impl.CssElementTypes;
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
    CssRuleset possibleParentRuleset = PsiTreeUtil.getParentOfType(element, CssRuleset.class);
    CssRuleset topRuleset = PsiTreeUtil.getParentOfType(possibleParentRuleset, CssRuleset.class);
    CssAtRule possibleParentAtRule = PsiTreeUtil.getParentOfType(element, CssAtRule.class);
    if (!isConditionalGroupAtRule(possibleParentAtRule)) {
      return topRuleset != null;
    }
    else {
      CssAtRule topAtRule = PsiTreeUtil.getParentOfType(topRuleset, CssAtRule.class);
      if (topRuleset != null && possibleParentAtRule.equals(topAtRule)) {
        return true;
      }
      else {
        return false;
      }
    }
  }

  @Contract("null -> false")
  public static boolean isConditionalGroupAtRule(@Nullable CssAtRule atRule) {
    if (atRule == null) return false;
    final ASTNode node = atRule.getNode();
    return CssElementTypes.CSS_MEDIA == node.getElementType() ||
           CssElementTypes.CSS_DOCUMENT_RULE == node.getElementType() ||
           CssElementTypes.CSS_SUPPORTS == node.getElementType();
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