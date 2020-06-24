// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2IvyDirectiveAttribute implements Angular2DirectiveAttribute {

  private final String myName;
  private final JSTypeDeclaration mySource;

  Angular2IvyDirectiveAttribute(@NotNull String name,
                                @NotNull JSTypeDeclaration source) {
    myName = name;
    mySource = source;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @Nullable JSType getType() {
    return mySource.getJSType();
  }

  @Override
  public @NotNull PsiElement getSourceElement() {
    return mySource;
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
