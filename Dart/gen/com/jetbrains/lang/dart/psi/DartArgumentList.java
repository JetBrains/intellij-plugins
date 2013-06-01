// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartArgumentList extends DartPsiCompositeElement {

  @NotNull
  List<DartExpression> getExpressionList();

  @NotNull
  List<DartNamedArgument> getNamedArgumentList();

}
