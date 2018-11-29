// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartSetLiteralExpression extends DartExpression, DartReference {

  @NotNull
  List<DartExpression> getExpressionList();

  @Nullable
  DartTypeArguments getTypeArguments();

}
