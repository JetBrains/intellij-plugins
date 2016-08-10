package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.completion.CssAddSpaceWithBracesInsertHandler;
import com.intellij.psi.css.util.CssCompletionUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.completion.handler.PostCssOneLineAtRuleInsertHandler;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

public class PostCssDumbAwareCompletionContributor extends CompletionContributor implements DumbAware {
  private static final PostCssOneLineAtRuleInsertHandler ONE_LINE_STATEMENT_HANDLER = new PostCssOneLineAtRuleInsertHandler();

  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull final CompletionResultSet result) {
    final CompletionResultSet resultSet = CssCompletionUtil.fixPrefixForVendorPrefixes(parameters, result);
    super.fillCompletionVariants(parameters, resultSet);
    if (!resultSet.isStopped()) {
      PsiElement position = parameters.getPosition();
      if (PostCssPsiUtil.isInsidePostCss(position) && position.getNode().getElementType() == CssElementTypes.CSS_ATKEYWORD) {
        resultSet.addElement(CssCompletionUtil.lookupForKeyword("@custom-selector", ONE_LINE_STATEMENT_HANDLER));
        resultSet.addElement(CssCompletionUtil.lookupForKeyword("@custom-media", ONE_LINE_STATEMENT_HANDLER));
        resultSet.addElement(CssCompletionUtil.lookupForKeyword("@apply", ONE_LINE_STATEMENT_HANDLER));
        addNest(position, resultSet);

        result.runRemainingContributors(parameters, r -> {
          if (!r.getLookupElement().getLookupString().equals("@apply")) resultSet.passResult(r);
        });
      }
    }
  }

  private static void addNest(@NotNull PsiElement position, @NotNull CompletionResultSet result) {
    PsiElement parent = position.getParent();
    if (parent instanceof PsiErrorElement) {
      parent = parent.getParent();
    }
    if (parent == null || parent.getNode().getElementType() != CssElementTypes.CSS_BAD_AT_RULE) return;

    PsiElement prev = parent.getPrevSibling();
    boolean insideBlock = parent.getParent() instanceof CssBlock && (prev == null || !(prev instanceof PsiErrorElement));
    boolean insideNestedRule = PsiTreeUtil.getParentOfType(parent, CssRuleset.class) != null;
    if (insideBlock && insideNestedRule) {
      result.addElement(CssCompletionUtil.lookupForKeyword("@nest", new CssAddSpaceWithBracesInsertHandler(false)));
    }
  }

}