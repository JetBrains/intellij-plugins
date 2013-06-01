package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public abstract class DartBlockAndChildStatementSurrounderBase<T extends PsiElement> extends DartBlockStatementSurrounderBase {
  @Nullable
  protected PsiElement findElementToDelete(PsiElement surrounder) {
    return PsiTreeUtil.getChildOfType(surrounder, getClassToDelete());
  }

  protected abstract Class<T> getClassToDelete();
}
