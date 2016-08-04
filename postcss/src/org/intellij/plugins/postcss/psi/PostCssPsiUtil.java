package org.intellij.plugins.postcss.psi;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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
  public static Collection<PsiElement> findAllAmpersands(@Nullable final PsiElement element) {
    if (element == null) return ContainerUtil.emptyList();
    PsiElementProcessor.CollectElements<PsiElement> processor = new PsiElementProcessor.CollectElements<PsiElement>() {
      @Override
      public boolean execute(@NotNull PsiElement each) {
        if (isAmpersand(each)) return super.execute(each);
        for (PsiElement child = each.getFirstChild(); child != null; child = child.getNextSibling()) {
          execute(child);
        }
        return true;
      }
    };
    processor.execute(element);
    return processor.getCollection();
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
    if (element == null) return false;
    return element.getNode().getElementType() == PostCssTokenTypes.AMPERSAND;
  }
}