// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartPatternVariableDeclaration extends DartPsiCompositeElement {

  @NotNull
  DartExpression getExpression();

  @Nullable
  DartListPattern getListPattern();

  @Nullable
  DartMapPattern getMapPattern();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartObjectPattern getObjectPattern();

  @Nullable
  DartParenthesizedPattern getParenthesizedPattern();

  @Nullable
  DartRecordPattern getRecordPattern();

}
