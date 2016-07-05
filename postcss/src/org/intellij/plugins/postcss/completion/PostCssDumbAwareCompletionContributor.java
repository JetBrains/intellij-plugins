package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.completion.provider.PostCssKeywordsCompletionProvider;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class PostCssDumbAwareCompletionContributor extends CompletionContributor implements DumbAware {

  public PostCssDumbAwareCompletionContributor() {
    extend(CompletionType.BASIC, nestKeyword(), new PostCssKeywordsCompletionProvider());
  }

  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull CompletionResultSet result) {
    result = fixPrefixForVendorPrefixes(parameters, result, CssElementTypes.CSS_ATKEYWORD);
    super.fillCompletionVariants(parameters, result);
  }

  private static ElementPattern<? extends PsiElement> nestKeyword() {
    return inPostCssFile(CssElementTypes.CSS_ATKEYWORD).inside(true, psiElement(CssElementTypes.CSS_RULESET));
  }

  private static PsiElementPattern.Capture<PsiElement> inPostCssFile(IElementType type) {
    return psiElement(type).inside(psiElement(PostCssElementTypes.POST_CSS_STYLESHEET));
  }

  /*TODO use CssDumbAwareCompletionContributor#fixPrefixForVendorPrefixes instead when PostCSS module will be part of API*/
  public static CompletionResultSet fixPrefixForVendorPrefixes(@NotNull CompletionParameters parameters,
                                                               @NotNull CompletionResultSet result,
                                                               @NotNull IElementType... typesToFix) {
    final PsiElement position = parameters.getPosition();
    IElementType type = position.getNode().getElementType();
    if (CssElementTypes.NAME_TOKEN_TYPES.contains(type) || type == CssElementTypes.CSS_ATKEYWORD
        || TokenSet.create(typesToFix).contains(type)) {
      final String positionText = position.getText();
      final int prefixShift = parameters.getOffset() - position.getTextRange().getStartOffset();
      if (0 < prefixShift && prefixShift < positionText.length()) {
        return result.withPrefixMatcher(result.getPrefixMatcher().cloneWithPrefix(positionText.substring(0, prefixShift)));
      }
    }
    return result;
  }
}