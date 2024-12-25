// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.IDartBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartBlockStatementSurrounderBase extends DartStatementSurrounder {
  @Override
  protected PsiElement findElementToAdd(@NotNull PsiElement surrounder) {
    final IDartBlock block = PsiTreeUtil.getChildOfType(surrounder, IDartBlock.class);
    return block == null ? null : block.getStatements();
  }

  @Override
  protected int cleanUpAndGetPlaceForCaret(@NotNull PsiElement surrounder) {
    final PsiElement childToDelete = findElementToDelete(surrounder); //true
    final int result = childToDelete == null ? surrounder.getTextRange().getStartOffset() : childToDelete.getTextRange().getStartOffset();
    if (childToDelete != null) {
      childToDelete.delete();
    }
    return result;
  }

  protected abstract @Nullable PsiElement findElementToDelete(PsiElement surrounder);
}
