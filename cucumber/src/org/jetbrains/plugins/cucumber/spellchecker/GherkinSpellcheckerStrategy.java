// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.spellchecker;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.spellchecker.quickfixes.SpellCheckerQuickFixFactory;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementType;

import java.util.List;

public final class GherkinSpellcheckerStrategy extends SpellcheckingStrategy implements DumbAware {
  @Override
  public @NotNull Tokenizer getTokenizer(final PsiElement element) {
    if (element instanceof LeafElement) {
      final ASTNode node = element.getNode();
      if (node != null && node.getElementType() instanceof GherkinElementType){
        return TEXT_TOKENIZER;
      }
    }
    return super.getTokenizer(element);
  }

  @Override
  public LocalQuickFix[] getRegularFixes(@NotNull PsiElement element,
                                         @NotNull TextRange textRange,
                                         boolean useRename,
                                         String typo) {
    List<LocalQuickFix> result = SpellCheckerQuickFixFactory.changeToVariants(element, textRange, typo);
    result.add(SpellCheckerQuickFixFactory.saveTo(element, textRange, typo));
    return result.toArray(LocalQuickFix.EMPTY_ARRAY);
  }
}