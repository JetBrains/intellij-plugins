// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface Angular2Element {

  @NotNull
  PsiElement getSourceElement();

  default @NotNull PsiElement getNavigableElement() {
    return getSourceElement();
  }
}
