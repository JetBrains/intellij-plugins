// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Angular2TemplateScopesProvider {

  static final ExtensionPointName<Angular2TemplateScopesProvider>
    EP_NAME = ExtensionPointName.create("org.angular2.templateScopesProvider");


  /**
   * If Angular expression is injected the @{code hostElement} is not null.
   */
  public abstract @NotNull List<? extends Angular2TemplateScope> getScopes(@NotNull PsiElement element,
                                                                           @Nullable PsiElement hostElement);
}
