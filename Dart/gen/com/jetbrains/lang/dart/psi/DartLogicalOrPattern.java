// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartLogicalOrPattern extends DartPsiCompositeElement {

  @NotNull
  List<DartConstantPattern> getConstantPatternList();

  @NotNull
  List<DartIdentifierPattern> getIdentifierPatternList();

  @NotNull
  List<DartListPattern> getListPatternList();

  @NotNull
  List<DartLogicalAndPattern> getLogicalAndPatternList();

  @Nullable
  DartLogicalOrPattern getLogicalOrPattern();

  @NotNull
  List<DartMapPattern> getMapPatternList();

  @NotNull
  List<DartObjectPattern> getObjectPatternList();

  @NotNull
  List<DartParenthesizedPattern> getParenthesizedPatternList();

  @NotNull
  List<DartRecordPattern> getRecordPatternList();

  @NotNull
  List<DartRelationalPattern> getRelationalPatternList();

  @NotNull
  List<DartUnaryPattern> getUnaryPatternList();

  @NotNull
  List<DartVariablePattern> getVariablePatternList();

}
