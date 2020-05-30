package com.jetbrains.lang.dart.ide.surroundWith;

import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.ide.surroundWith.statement.*;
import com.jetbrains.lang.dart.util.DartRefactoringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartStatementsSurroundDescriptor implements SurroundDescriptor {
  @Override
  public PsiElement @NotNull [] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
    final PsiElement[] statements = DartRefactoringUtil.findStatementsInRange(file, startOffset, endOffset);
    if (statements == null) return PsiElement.EMPTY_ARRAY;
    return statements;
  }

  @Override
  public Surrounder @NotNull [] getSurrounders() {
    return new Surrounder[]{
      new DartWithIfSurrounder(),
      new DartWithIfElseSurrounder(),
      new DartWithWhileSurrounder(),
      new DartWithDoWhileSurrounder(),
      new DartWithForSurrounder(),
      new DartWithTryCatchSurrounder(),
      new DartWithTryCatchFinallySurrounder()
    };
  }

  @Override
  public boolean isExclusive() {
    return false;
  }
}
