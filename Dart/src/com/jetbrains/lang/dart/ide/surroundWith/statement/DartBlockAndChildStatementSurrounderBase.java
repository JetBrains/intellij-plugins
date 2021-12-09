// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public abstract class DartBlockAndChildStatementSurrounderBase<T extends PsiElement> extends DartBlockStatementSurrounderBase {
  @Override
  @Nullable
  protected PsiElement findElementToDelete(PsiElement surrounder) {
    return PsiTreeUtil.getChildOfType(surrounder, getClassToDelete());
  }

  protected abstract Class<T> getClassToDelete();
}
