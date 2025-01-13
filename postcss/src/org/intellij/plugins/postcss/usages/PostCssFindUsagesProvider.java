package org.intellij.plugins.postcss.usages;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.ElementDescriptionUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.tree.TokenSet;
import com.intellij.usageView.UsageViewLongNameLocation;
import com.intellij.usageView.UsageViewShortNameLocation;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.lexer.PostCssLexer;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;
import org.jetbrains.annotations.NotNull;

public final class PostCssFindUsagesProvider implements FindUsagesProvider {
  @Override
  public @NotNull WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(new PostCssLexer(), TokenSet.create(CssElementTypes.CSS_IDENT), PostCssTokenTypes.POST_CSS_COMMENTS,
                                   TokenSet.create(CssElementTypes.CSS_STRING_TOKEN));
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof PostCssCustomSelector ||
           psiElement instanceof PostCssCustomMedia ||
           psiElement instanceof PostCssSimpleVariableDeclaration;
  }

  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return HelpID.FIND_OTHER_USAGES;
  }

  @Override
  public @NotNull String getType(@NotNull PsiElement element) {
    if (element instanceof PostCssCustomSelector) {
      return PostCssBundle.message("find.usages.element.type.custom.selector");
    }
    else if (element instanceof PostCssCustomMedia) {
      return PostCssBundle.message("find.usages.element.type.custom.media");
    }
    else if (element instanceof PostCssSimpleVariableDeclaration) {
      return PostCssBundle.message("find.usages.element.type.simple.var");
    }
    return "";
  }

  @Override
  public @NotNull String getDescriptiveName(@NotNull PsiElement element) {
    return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE);
  }

  @Override
  public @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    return ElementDescriptionUtil.getElementDescription(element, UsageViewShortNameLocation.INSTANCE);
  }
}