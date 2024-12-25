// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.jhipster.psi.*;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class JdlFindUsagesProvider implements FindUsagesProvider {
  public static final @NlsSafe String CONSTANT = "constant";
  public static final @NlsSafe String ENUM = "enum";
  public static final @NlsSafe String ENTITY = "entity";

  @Override
  public @NotNull WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(new JdlLexer(),
                                   TokenSet.create(JdlTokenTypes.IDENTIFIER),
                                   JdlTokenSets.COMMENTS,
                                   TokenSet.EMPTY);
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof JdlEntity
           || psiElement instanceof JdlEnum
           || psiElement instanceof JdlConstant;
  }

  @Override
  public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @Override
  public @Nls @NotNull String getType(@NotNull PsiElement element) {
    if (element instanceof JdlEntity) {
      return ENTITY;
    }
    if (element instanceof JdlEnum) {
      return ENUM;
    }
    if (element instanceof JdlConstant) {
      return CONSTANT;
    }
    return "";
  }

  @Override
  public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
    String name = element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : null;
    return name != null ? name : JdlBundle.message("unnamed");
  }

  @Override
  public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    return getDescriptiveName(element);
  }
}
