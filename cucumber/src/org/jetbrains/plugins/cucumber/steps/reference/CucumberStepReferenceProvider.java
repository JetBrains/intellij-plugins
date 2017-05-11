package org.jetbrains.plugins.cucumber.steps.reference;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementTypes;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;

public class CucumberStepReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (element instanceof GherkinStepImpl) {

      TokenSet textAndParamSet = TokenSet.create(GherkinTokenTypes.TEXT, GherkinTokenTypes.STEP_PARAMETER_TEXT,
                                                 GherkinTokenTypes.STEP_PARAMETER_BRACE, GherkinElementTypes.STEP_PARAMETER);
      ASTNode textNode = element.getNode().findChildByType(textAndParamSet);
      textAndParamSet = TokenSet.orSet(textAndParamSet, TokenSet.create(TokenType.WHITE_SPACE));
      if (textNode != null) {
        int start = textNode.getTextRange().getStartOffset();
        int end = textNode.getTextRange().getEndOffset();
        int endBeforeSpace = end;
        textNode = textNode.getTreeNext();
        while (textNode != null && textAndParamSet.contains(textNode.getElementType())) {
          if (textNode.getElementType() == TokenType.WHITE_SPACE) {
            endBeforeSpace = end;
          } else {
            endBeforeSpace = textNode.getTextRange().getEndOffset();
          }
          end = textNode.getTextRange().getEndOffset();
          textNode = textNode.getTreeNext();
        }
        TextRange tr = new TextRange(start, endBeforeSpace);
        CucumberStepReference reference =  new CucumberStepReference(element, tr.shiftRight(-element.getTextOffset()));
        return new PsiReference[] {reference};
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }
}
