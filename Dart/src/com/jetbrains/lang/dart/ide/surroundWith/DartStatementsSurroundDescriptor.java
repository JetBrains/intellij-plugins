package com.jetbrains.lang.dart.ide.surroundWith;

import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.ide.surroundWith.statement.DartWithDoWhileSurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.statement.DartWithForSurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.statement.DartWithIfElseSurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.statement.DartWithIfSurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.statement.DartWithTryCatchFinallySurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.statement.DartWithTryCatchSurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.statement.DartWithWhileSurrounder;
import com.jetbrains.lang.dart.util.DartRefactoringUtil;
import org.jetbrains.annotations.NotNull;

public final class DartStatementsSurroundDescriptor implements SurroundDescriptor {
  @Override
  public PsiElement @NotNull [] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
    return DartRefactoringUtil.findStatementsInRange(file, startOffset, endOffset);
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
