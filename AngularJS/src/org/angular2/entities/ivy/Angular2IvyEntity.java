// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.angular2.entities.source.Angular2SourceEntityBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Angular2IvyEntity extends Angular2SourceEntityBase {

  protected final TypeScriptField myDefField;

  protected Angular2IvyEntity(@NotNull TypeScriptField defField) {
    super(Objects.requireNonNull(PsiTreeUtil.getContextOfType(defField, TypeScriptClass.class)));
    myDefField = defField;
  }

  @Nullable
  @Override
  public ES6Decorator getDecorator() {
    return null;
  }

  @NotNull
  @Override
  public PsiElement getNavigableElement() {
    return myDefField;
  }

  @NotNull
  @Override
  public PsiElement getSourceElement() {
    return myDefField;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2IvyEntity entity = (Angular2IvyEntity)o;
    return myDefField.equals(entity.myDefField) &&
           myClass.equals(entity.myClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myDefField, myClass);
  }
}
