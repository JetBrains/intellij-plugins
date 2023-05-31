package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.completion.provider.PropertyNamesCompletionProvider;
import com.intellij.psi.css.impl.util.completion.provider.TagsCompletionProvider;
import com.intellij.psi.css.util.CssCompletionUtil;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssStubElementTypes;
import org.intellij.plugins.postcss.completion.handler.PostCssOneLineAtRuleInsertHandler;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.psi.css.impl.util.completion.CssDumbAwareCompletionContributor.*;

public class PostCssDumbAwareCompletionContributor extends CompletionContributor implements DumbAware {
  private static final PostCssOneLineAtRuleInsertHandler ONE_LINE_STATEMENT_HANDLER = new PostCssOneLineAtRuleInsertHandler();

  public PostCssDumbAwareCompletionContributor() {
    extend(CompletionType.BASIC, selector(), new TagsCompletionProvider());
    extend(CompletionType.BASIC, propertyDeclaration(), new PropertyNamesCompletionProvider());
  }

  private static ElementPattern<? extends PsiElement> propertyDeclaration() {
    return inPostCssFile(CssElementTypes.CSS_IDENT)
      .andOr(propertyName(),
             psiElement().withParent(CssSimpleSelector.class).inside(CssBlock.class)
               .afterLeafSkipping(or(emptyElement(), spaceElement()), blockStartOrEnd())
               .beforeLeafSkipping(or(emptyElement(), spaceElement()), blockStartOrEnd()));
  }

  private static ElementPattern<PsiElement> selector() {
    return inPostCssFile(CssElementTypes.CSS_IDENT).andOr(propertyName(), propertyName());
  }

  private static PsiElementPattern.Capture<PsiElement> inPostCssFile(@NotNull IElementType type) {
    return psiElement(type).inside(psiElement(PostCssStubElementTypes.POST_CSS_STYLESHEET));
  }

  @Override
  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull final CompletionResultSet result) {
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