// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public interface CfmlParameter extends PsiElement, PsiNamedElement {
  boolean isRequired();

  @Nullable
  String getType();
  
  @Nullable
  String getDefault();

  @Override
  @Nullable
  @NonNls
  String getName();
  
  @Nullable
  String getDescription();
}
