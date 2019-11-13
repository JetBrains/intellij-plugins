// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public abstract class Angular2IvyEntity extends UserDataHolderBase implements Angular2Entity {

  protected final TypeScriptField myDefField;
  private final TypeScriptClass myClass;

  protected Angular2IvyEntity(@NotNull TypeScriptField defField) {
    myDefField = defField;
    myClass = PsiTreeUtil.getContextOfType(myDefField, TypeScriptClass.class);
    assert myClass != null;
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

  @NotNull
  @Override
  public String getName() {
    return StringUtil.notNullize(myClass.getName(), Angular2Bundle.message("angular.description.unnamed"));
  }

  @NotNull
  @Override
  public TypeScriptClass getTypeScriptClass() {
    return myClass;
  }

  protected <T> T getCachedValue(@NotNull CachedValueProvider<T> provider) {
    return CachedValuesManager.getManager(myDefField.getProject()).getCachedValue(this, provider);
  }

  @NotNull
  protected Collection<Object> getClassModificationDependencies() {
    return getCachedValue(() -> {
      Collection<Object> dependencies = new HashSet<>();
      JSClassUtils.processClassesInHierarchy(myClass, true, (aClass, typeSubstitutor, fromImplements) -> {
        dependencies.add(aClass);
        return true;
      });
      return CachedValueProvider.Result.create(dependencies, dependencies);
    });
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
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
