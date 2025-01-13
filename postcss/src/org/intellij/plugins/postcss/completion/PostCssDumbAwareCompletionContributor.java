package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.completion.CssDumbAwareCompletionContributor;
import com.intellij.psi.css.impl.util.completion.provider.PropertyNamesCompletionProvider;
import com.intellij.psi.css.impl.util.completion.provider.TagsCompletionProvider;
import com.intellij.psi.css.util.CssCompletionUtil;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssStubElementTypes;
import org.intellij.plugins.postcss.completion.handler.PostCssOneLineAtRuleInsertHandler;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.StandardPatterns.or;

public final class PostCssDumbAwareCompletionContributor extends CompletionContributor implements DumbAware {
  private static final PostCssOneLineAtRuleInsertHandler ONE_LINE_STATEMENT_HANDLER = new PostCssOneLineAtRuleInsertHandler();

  public PostCssDumbAwareCompletionContributor() {
    extend(CompletionType.BASIC, selector().andNot(CssDumbAwareCompletionContributor.elementInsidePropertyAtRule()), new TagsCompletionProvider());
    extend(CompletionType.BASIC, propertyDeclaration(), new PropertyNamesCompletionProvider());
  }

  private static ElementPattern<? extends PsiElement> propertyDeclaration() {
    return inPostCssFile(CssElementTypes.CSS_IDENT)
      .andOr(CssDumbAwareCompletionContributor.propertyName(),
             PlatformPatterns.psiElement().withParent(CssSimpleSelector.class).inside(CssBlock.class)
               .afterLeafSkipping(or(CssDumbAwareCompletionContributor.emptyElement(), CssDumbAwareCompletionContributor.spaceElement()), CssDumbAwareCompletionContributor.blockStartOrEnd())
               .beforeLeafSkipping(or(CssDumbAwareCompletionContributor.emptyElement(), CssDumbAwareCompletionContributor.spaceElement()), CssDumbAwareCompletionContributor.blockStartOrEnd()));
  }

  private static PsiElementPattern.Capture<PsiElement> selector() {
    return inPostCssFile(CssElementTypes.CSS_IDENT).andOr(CssDumbAwareCompletionContributor.propertyName(), CssDumbAwareCompletionContributor.propertyName());
  }

  private static PsiElementPattern.Capture<PsiElement> inPostCssFile(@NotNull IElementType type) {
    return PlatformPatterns.psiElement(type).inside(PlatformPatterns.psiElement(PostCssStubElementTypes.POST_CSS_STYLESHEET));
  }

  @Override
  public void fillCompletionVariants(final @NotNull CompletionParameters parameters, final @NotNull CompletionResultSet result) {
    final CompletionResultSet resultSet = CssCompletionUtil.fixPrefixForVendorPrefixes(parameters, result);
    super.fillCompletionVariants(parameters, resultSet);
    if (!resultSet.isStopped()) {
      PsiElement position = parameters.getPosition();
      if (PostCssPsiUtil.isInsidePostCss(position) && position.getNode().getElementType() == CssElementTypes.CSS_ATKEYWORD) {
        resultSet.addElement(CssCompletionUtil.lookupForKeyword("@custom-selector", ONE_LINE_STATEMENT_HANDLER));
        resultSet.addElement(CssCompletionUtil.lookupForKeyword("@custom-media", ONE_LINE_STATEMENT_HANDLER));
      }
    }
  }
}