// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsAccumulateFunction extends DroolsPsiCompositeElement {

  @NotNull
  DroolsAccumulateParameters getAccumulateParameters();

  @NotNull
  DroolsFunctionName getFunctionName();

  @Nullable
  DroolsLabel getLabel();

}
