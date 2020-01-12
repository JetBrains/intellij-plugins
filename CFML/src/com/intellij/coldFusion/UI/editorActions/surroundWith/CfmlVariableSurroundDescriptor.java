/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class CfmlVariableSurroundDescriptor implements SurroundDescriptor {
  public static final Surrounder[] SURROUNDERS = new Surrounder[]{
    new CfmlSharpSurrounder()
  };

  @Override
  public PsiElement @NotNull [] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
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

  @Override
  public Surrounder @NotNull [] getSurrounders() {
    return SURROUNDERS;
  }

  @Override
  public boolean isExclusive() {
    return false;
  }
}
