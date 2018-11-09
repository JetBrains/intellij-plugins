// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class Angular2LanguageConfigurableProvider extends JSInheritedLanguagesConfigurableProvider {
  @Override
  public boolean isNeedToBeTerminated(@NotNull PsiElement element) {
    return false;
  }
}
