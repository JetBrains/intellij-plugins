package org.intellij.plugins.postcss.psi;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssElement;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.css.impl.CssElementTypes;
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

  public static boolean isInsidePostCss(@Nullable CssElement element) {
    return CssPsiUtil.getStylesheetLanguage(element) == PostCssLanguage.INSTANCE;
  }

  @Contract("null -> true")
  public static boolean isEmptyElement(@Nullable PsiElement element) {
    return element == null || element.getTextLength() == 0;
  }

  @Contract("null -> null")
  public static PsiElement getParentNonConditionalAtRuleOrRuleset(@Nullable PsiElement element) {
    CssElement parent = PsiTreeUtil.getParentOfType(element, CssRuleset.class, CssAtRule.class);
    return isConditionalGroupAtRule(parent) ? null : parent;
  }

  @Contract("null -> false")
  public static boolean isInsideNestedRuleset(@Nullable PsiElement element) {
    PsiElement atRuleOrRuleset = getParentNonConditionalAtRuleOrRuleset(element);
    return getParentNonConditionalAtRuleOrRuleset(atRuleOrRuleset) != null;
  }

  @Contract("null -> false")
  private static boolean isConditionalGroupAtRule(@Nullable PsiElement element) {
    if (element == null || !(element instanceof CssAtRule)) return false;
    final ASTNode node = element.getNode();
    return CssElementTypes.CSS_MEDIA == node.getElementType() ||
           CssElementTypes.CSS_DOCUMENT_RULE == node.getElementType() ||
           CssElementTypes.CSS_SUPPORTS == node.getElementType();
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

  @Contract("null -> false")
  public static boolean isNestSym(@Nullable PsiElement element) {
    if (element == null) return false;
    PsiElement firstChild = element.getFirstChild();
    return firstChild instanceof LeafElement && ((LeafElement)firstChild).getElementType() == PostCssTokenTypes.POST_CSS_NEST_SYM;
  }
}