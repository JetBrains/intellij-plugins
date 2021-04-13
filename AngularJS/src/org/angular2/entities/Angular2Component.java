// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Angular2Component extends Angular2Directive {

  @Nullable
  PsiFile getTemplateFile();

  @NotNull
  List<PsiFile> getCssFiles();

  @NotNull
  List<Angular2DirectiveSelector> getNgContentSelectors();

  @Override
  default boolean isComponent() {
    return true;
  }
}
