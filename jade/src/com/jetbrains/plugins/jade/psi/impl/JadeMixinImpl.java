package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JadeMixinImpl extends JadeTagImpl implements PsiNameIdentifierOwner {
  public JadeMixinImpl() {
    super(JadeElementTypes.MIXIN);
  }

  @Override
  public @NotNull String getName() {
    PsiElement nameIdentifier = getNameIdentifier();
    return nameIdentifier != null ? nameIdentifier.getText() : "";
  }

  @Override
  public PsiReference @NotNull [] getReferences(@NotNull PsiReferenceService.Hints hints) {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }

  @Override
  public int getTextOffset() {
    PsiElement nameIdentifier = getNameIdentifier();
    if (nameIdentifier != null) {
      return nameIdentifier.getTextOffset();
    }
    else {
      return super.getTextOffset();
    }
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    return findPsiChildByType(JadeTokenTypes.TAG_NAME);
  }

  @Override
  public @NotNull Language getLanguage() {
    return super.getLanguage();
  }
}
