// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public abstract class DartBlockAndChildStatementSurrounderBase<T extends PsiElement> extends DartBlockStatementSurrounderBase {
  @Override
  protected @Nullable PsiElement findElementToDelete(PsiElement surrounder) {
    return PsiTreeUtil.getChildOfType(surrounder, getClassToDelete());
  }

  protected abstract Class<T> getClassToDelete();
}
