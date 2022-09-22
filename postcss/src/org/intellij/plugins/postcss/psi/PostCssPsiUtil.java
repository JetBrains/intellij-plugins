package org.intellij.plugins.postcss.psi;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssMediaFeature;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PostCssPsiUtil {
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
  public static boolean isInsideRulesetWithNestedRulesets(@Nullable PsiElement element) {
    CssRuleset ruleset = PsiTreeUtil.getParentOfType(element, CssRuleset.class);
    return ruleset != null && PsiTreeUtil.findChildOfAnyType(ruleset.getBlock(), false, CssRuleset.class) != null;
  }

  @Contract("null -> false")
  public static boolean isInsideCustomSelector(@Nullable PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PostCssCustomSelectorAtRule.class) != null;
  }

  @Contract("null -> false")
  public static boolean containsAmpersand(@Nullable PsiElement element) {
    return element != null && element.textContains('&');
  }

  @NotNull
  public static List<TextRange> findAllAmpersands(@Nullable final PsiElement element) {
    if (element == null) return Collections.emptyList();
    List<TextRange> result = new ArrayList<>();
    int index, offset = 0;
    String text = element.getText();
    do {
      index = text.indexOf('&', offset);
      if (index >= 0) {
        offset = index + 1;
        result.add(TextRange.create(offset - 1, offset));
      }
    }
    while (index >= 0);
    return result;
  }

  @NotNull
  public static List<? extends PsiElement> findAllComparisonOperators(@Nullable final CssMediaFeature mediaFeature) {
    return SyntaxTraverser.psiTraverser(mediaFeature).filter(PostCssPsiUtil::isComparisonOperator).toList();
  }

  public static boolean startsWithAmpersand(@NotNull PsiElement element) {
    return StringUtil.startsWithChar(element.getText(), '&');
  }

  @Contract("null -> false")
  public static boolean isInsideNest(@Nullable PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PostCssNest.class, CssRuleset.class, CssAtRule.class) instanceof PostCssNest;
  }

  @Contract("null -> false")
  public static boolean isComparisonOperator(@Nullable PsiElement element) {
    return element != null && CssElementTypes.COMPARISON_OPERATORS.contains(element.getNode().getElementType());
  }
}