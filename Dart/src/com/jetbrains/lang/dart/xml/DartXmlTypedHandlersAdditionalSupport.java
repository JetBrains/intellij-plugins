// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.XmlTypedHandlersAdditionalSupport;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;

public final class DartXmlTypedHandlersAdditionalSupport implements XmlTypedHandlersAdditionalSupport {
  
  @Override
  public boolean isAvailable(@NotNull PsiFile psiFile, @NotNull Language lang) {
    return lang.isKindOf(DartLanguage.INSTANCE);
  }
}