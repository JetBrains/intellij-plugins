// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartForLoopPartsInBraces;
import org.jetbrains.annotations.Nullable;

public class DartWithForSurrounder extends DartBlockAndChildStatementSurrounderBase<DartForLoopPartsInBraces> {
  @Override
  public String getTemplateDescription() {
    @NlsSafe String description = "for";
    return description;
  }

  @Override
  protected String getTemplateText() {
    return "for(a in []){\n}";
  }

  @Override
  protected Class<DartForLoopPartsInBraces> getClassToDelete() {
    return DartForLoopPartsInBraces.class;
  }

  @Override
  protected @Nullable PsiElement findElementToDelete(PsiElement surrounder) {
    PsiElement result = super.findElementToDelete(surrounder);
    return result instanceof DartForLoopPartsInBraces ? ((DartForLoopPartsInBraces)result).getForLoopParts() : null;
  }
}
