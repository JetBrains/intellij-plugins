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

public abstract class Angular2IvyEntity<T extends Angular2IvySymbolDef.Entity> extends Angular2SourceEntityBase {

  protected final T myEntityDef;

  protected Angular2IvyEntity(@NotNull T entityDef) {
    super(Objects.requireNonNull(PsiTreeUtil.getContextOfType(entityDef.getField(), TypeScriptClass.class)));
    myEntityDef = entityDef;
  }

  protected TypeScriptField getField() {
    return myEntityDef.getField();
  }

  @Override
  public @Nullable ES6Decorator getDecorator() {
    return null;
  }

  @Override
  public @NotNull PsiElement getSourceElement() {
    return getField();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2IvyEntity<?> entity = (Angular2IvyEntity<?>)o;
    return getField().equals(entity.getField()) &&
           myClass.equals(entity.myClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getField(), myClass);
  }
}
