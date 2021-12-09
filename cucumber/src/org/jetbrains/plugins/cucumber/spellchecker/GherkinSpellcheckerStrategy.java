// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.spellchecker;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.spellchecker.quickfixes.ChangeTo;
import com.intellij.spellchecker.quickfixes.SaveTo;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementType;

import java.util.ArrayList;

public class GherkinSpellcheckerStrategy extends SpellcheckingStrategy {
  @NotNull
  @Override
  public Tokenizer getTokenizer(final PsiElement element) {
    if (element instanceof LeafElement) {
      final ASTNode node = element.getNode();
      if (node != null && node.getElementType() instanceof GherkinElementType){
        return SpellcheckingStrategy.TEXT_TOKENIZER;
      }
    }
    return super.getTokenizer(element);
  }

  @Override
  public LocalQuickFix[] getRegularFixes(PsiElement element,
                                         @NotNull TextRange textRange,
                                         boolean useRename,
                                         String typo) {
    ArrayList<LocalQuickFix> result = new ArrayList<>(new ChangeTo(typo, element, textRange).getAllAsFixes());
    result.add(new SaveTo(typo));
    return result.toArray(LocalQuickFix.EMPTY_ARRAY);
  }
}