// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import one.util.streamex.StreamEx;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

public abstract class Angular2SourceEntity extends UserDataHolderBase implements Angular2Entity {

  private final ES6Decorator myDecorator;
  private final JSImplicitElement myImplicitElement;
  private final TypeScriptClass myClass;

  public Angular2SourceEntity(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    this.myDecorator = decorator;
    myImplicitElement = implicitElement;
    myClass = PsiTreeUtil.getContextOfType(myDecorator, TypeScriptClass.class);
    assert myClass != null;
  }

  @NotNull
  @Override
  public JSElement getNavigableElement() {
    return myDecorator;
  }

  @Override
  @NotNull
  public JSElement getSourceElement() {
    // try to find a fresh implicit element
    return StreamEx.ofNullable(myDecorator.getIndexingData())
      .map(JSElementIndexingData::getImplicitElements)
      .nonNull()
      .flatCollection(Function.identity())
      .filter(el -> myImplicitElement.getName().equals(el.getName())
                    && Objects.equals(myImplicitElement.getUserString(), el.getUserString()))
      .findFirst()
      .orElse(myImplicitElement);
  }

  @NotNull
  @Override
  public ES6Decorator getDecorator() {
    return myDecorator;
  }

  @NotNull
  @Override
  public TypeScriptClass getTypeScriptClass() {
    return myClass;
  }

  @NotNull
  @Override
  public String getName() {
    return StringUtil.notNullize(myClass.getName(), Angular2Bundle.message("angular.description.unnamed"));
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }

  protected <T> T getCachedValue(@NotNull CachedValueProvider<T> provider) {
    return CachedValuesManager.getManager(myDecorator.getProject()).getCachedValue(this, provider);
  }

  @NotNull
  protected Collection<Object> getClassModificationDependencies() {
    return getCachedValue(() -> {
      Collection<Object> dependencies = new HashSet<>();
      JSClass cls = PsiTreeUtil.getContextOfType(myDecorator, JSClass.class);
      assert cls != null;
      JSClassUtils.processClassesInHierarchy(cls, true, (aClass, typeSubstitutor, fromImplements) -> {
        dependencies.add(aClass.getContainingFile());
        return true;
      });
      return CachedValueProvider.Result.create(dependencies, dependencies);
    });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2SourceEntity entity = (Angular2SourceEntity)o;
    return myDecorator.equals(entity.myDecorator) &&
           myClass.equals(entity.myClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myDecorator, myClass);
  }
}
