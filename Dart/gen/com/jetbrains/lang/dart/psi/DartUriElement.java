// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;

public interface DartUriElement extends DartPsiCompositeElement {

  @NotNull
  DartStringLiteralExpression getStringLiteralExpression();

  @NotNull
  Pair<String, TextRange> getUriStringAndItsRange();

}
