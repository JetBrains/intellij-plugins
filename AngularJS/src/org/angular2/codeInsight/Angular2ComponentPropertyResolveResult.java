// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2ComponentPropertyResolveResult extends JSResolveResult {

  private final JSRecordType.PropertySignature myPropertySignature;

  public Angular2ComponentPropertyResolveResult(@NotNull PsiElement element,
                                                @NotNull JSRecordType.PropertySignature propertySignature) {
    super(element);
    myPropertySignature = propertySignature;
  }

  public @Nullable JSType getJSType() {
    return myPropertySignature.getJSType();
  }

  public Angular2ComponentPropertyResolveResult copyWith(@NotNull PsiElement element) {
    return new Angular2ComponentPropertyResolveResult(element, myPropertySignature);
  }

  @Override
  public String toString() {
    return "Angular2ComponentPropertyResolveResult{" +
           "myElement=" + getElement() +
           ", myType=" + getJSType() +
           '}';
  }
}
