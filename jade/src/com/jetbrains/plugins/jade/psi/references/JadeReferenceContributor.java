// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.stubs.JadeStubElementTypes;
import org.jetbrains.annotations.NotNull;

public final class JadeReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

    registrar.registerReferenceProvider(PlatformPatterns.psiElement().withElementType(TokenSet.create(
      JadeElementTypes.MIXIN, JadeStubElementTypes.MIXIN_DECLARATION
    )), new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (!(element instanceof PsiNameIdentifierOwner)) {
          return PsiReference.EMPTY_ARRAY;
        }
        PsiElement nameIdentifier = ((PsiNameIdentifierOwner)element).getNameIdentifier();
        if (nameIdentifier == null) {
          return PsiReference.EMPTY_ARRAY;
        }

        int textOffset = nameIdentifier.getStartOffsetInParent();
        int textLength = nameIdentifier.getTextRange().getLength();

        return new PsiReference[]{ new JadeMixinReference(((PsiNameIdentifierOwner)element), TextRange.create(textOffset, textOffset + textLength)) };
      }
    });
  }
}
