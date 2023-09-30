// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.jhipster.psi.JdlApplication;
import com.intellij.jhipster.psi.JdlTokenTypes;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;

import static com.intellij.jhipster.psi.JdlTokenSets.TOP_LEVEL_BLOCKS;
import static com.intellij.patterns.PlatformPatterns.psiElement;

public final class JdlPatterns {
  private JdlPatterns() {
  }

  public static PsiElementPattern.Capture<PsiElement> jdlIdentifier() {
    return psiElement(JdlTokenTypes.IDENTIFIER);
  }

  public static PsiElementPattern.Capture<JdlApplication> jdlApplicationBlock() {
    return psiElement(JdlApplication.class);
  }

  public static PsiElementPattern.Capture<PsiElement> jdlTopLevelBlock() {
    return psiElement().withElementType(TOP_LEVEL_BLOCKS);
  }
}
