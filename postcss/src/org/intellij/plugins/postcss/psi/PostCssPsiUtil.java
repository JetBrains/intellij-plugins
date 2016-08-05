package org.intellij.plugins.postcss.psi;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

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
  public static boolean containsAmpersand(@Nullable PsiElement element) {
    return !findAllAmpersands(element).isEmpty();
  }

  @NotNull
  public static Collection<? extends PsiElement> findAllAmpersands(@Nullable final PsiElement element) {
    return SyntaxTraverser.psiTraverser(element).filter(PostCssPsiUtil::isAmpersand).toList();
  }

  @NotNull
  public static List<? extends PsiElement> findAllOperatorSigns(@Nullable final PsiElement element) {
    return SyntaxTraverser.psiTraverser(element).filter(PostCssPsiUtil::isOperatorSign).toList();
  }

  @Contract("null -> false")
  public static boolean startsWithAmpersand(@NotNull PsiElement selector) {
    return isAmpersand(PsiTreeUtil.getDeepestFirst(selector));
  }

  @Contract("null -> false")
  public static boolean isInsideNest(@Nullable PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PostCssNest.class, CssRuleset.class, CssAtRule.class) instanceof PostCssNest;
  }

  @Contract("null -> false")
  public static boolean isAmpersand(@Nullable PsiElement element) {
    return element != null && element.getNode().getElementType() == PostCssTokenTypes.AMPERSAND;
  }

  @Contract("null -> false")
  public static boolean isOperatorSign(@Nullable PsiElement element) {
    return element != null && PostCssTokenTypes.OPERATORS.contains(element.getNode().getElementType());
  }
}