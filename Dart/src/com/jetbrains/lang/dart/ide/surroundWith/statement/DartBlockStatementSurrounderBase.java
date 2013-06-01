package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public abstract class DartBlockStatementSurrounderBase extends DartStatementSurrounder {
  @Override
  protected PsiElement findElementToAdd(@NotNull PsiElement surrounder) {
    final DartBlock block = PsiTreeUtil.getChildOfType(surrounder, DartBlock.class);
    return block == null ? null : block.getStatements();
  }

  @Override
  protected int cleanUpAndGetPlaceForCaret(@NotNull PsiElement surrounder) {
    final PsiElement childToDelete = findElementToDelete(surrounder); //true
    final int result = childToDelete == null ? surrounder.getTextOffset() : childToDelete.getTextOffset();
    if (childToDelete != null) {
      childToDelete.delete();
    }
    return result;
  }

  @Nullable
  protected abstract PsiElement findElementToDelete(PsiElement surrounder);
}
