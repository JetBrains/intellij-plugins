// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.ognl.psi.resolve.variable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.light.LightVariableBuilder;
import org.jetbrains.annotations.NotNull;

public final class OgnlVariableReference extends LightVariableBuilder {
  public OgnlVariableReference(@NotNull String name,
                               @NotNull String type,
                               @NotNull String originInfo,
                               @NotNull PsiElement navigationElement) {
    super(name, type, navigationElement);
    setOriginInfo(originInfo);
  }

}