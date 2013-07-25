package com.intellij.coldFusion.UI.editorActions.surroundWith;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.lang.Language;
import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 11/28/11
 */
public class CfmlVariableSurroundDescriptor implements SurroundDescriptor {
  public static final Surrounder[] SURROUNDERS = new Surrounder[]{
    new CfmlSharpSurrounder()
  };

  @NotNull
  @Override
  public PsiElement[] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
    final FileViewProvider viewProvider = file.getViewProvider();
    PsiElement elementAt = PsiTreeUtil.findElementOfClassAtRange(file, startOffset, endOffset, CfmlReferenceExpression.class);
    if (elementAt != null) {
      return !(elementAt.getParent() instanceof CfmlReferenceExpression) ? new PsiElement[]{elementAt} : PsiElement.EMPTY_ARRAY;
    }
    for (Language language : viewProvider.getLanguages()) {
      elementAt = viewProvider.findElementAt(startOffset, language);
      if (elementAt != null &&
          (!language.is(CfmlLanguage.INSTANCE) || elementAt.getNode().getElementType() == CfmlTokenTypes.STRING_TEXT) &&
          elementAt.getNode().getText().matches("(\\S)+")
          &&
          elementAt.getTextRange().getStartOffset() == startOffset &&
          elementAt.getTextRange().getEndOffset() == endOffset) {
        return new PsiElement[]{elementAt};
      }
    }
    return PsiElement.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public Surrounder[] getSurrounders() {
    return SURROUNDERS;
  }

  @Override
  public boolean isExclusive() {
    return false;
  }
}
