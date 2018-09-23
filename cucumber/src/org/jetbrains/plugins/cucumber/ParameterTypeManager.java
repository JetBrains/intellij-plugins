// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParameterTypeManager {
  /**
   * @return value of Parameter Type with name
   */
  @Nullable
  String getParameterTypeValue(@NotNull String name);

  /**
   * @return element (String Literal) that declares Parameter Type with name
   */
  @Nullable
  PsiElement getParameterTypeDeclaration(@NotNull String name);
}
