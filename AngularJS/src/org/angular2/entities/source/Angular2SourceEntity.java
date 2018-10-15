// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.angular2.entities.Angular2Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public abstract class Angular2SourceEntity extends UserDataHolderBase implements Angular2Entity {

  private final ES6Decorator mySource;
  private final TypeScriptClass myClass;

  public Angular2SourceEntity(@NotNull ES6Decorator source) {
    this.mySource = source;
    myClass = PsiTreeUtil.getContextOfType(mySource, TypeScriptClass.class);
    assert myClass != null;
  }

  @NotNull
  @Override
  public JSElement getNavigableElement() {
    return mySource;
  }

  @NotNull
  @Override
  public ES6Decorator getDecorator() {
    return mySource;
  }

  @NotNull
  @Override
  public TypeScriptClass getTypeScriptClass() {
    return myClass;
  }

  protected <T> T getCachedValue(@NotNull CachedValueProvider<T> provider) {
    return CachedValuesManager.getManager(mySource.getProject()).getCachedValue(this, provider);
  }

  @NotNull
  protected Collection<Object> getClassModificationDependencies() {
    return getCachedValue(() -> {
      Collection<Object> dependencies = new HashSet<>();
      JSClass cls = PsiTreeUtil.getParentOfType(mySource, JSClass.class);
      assert cls != null;
      JSClassUtils.processClassesInHierarchy(cls, true, (aClass, typeSubstitutor, fromImplements) -> {
        dependencies.add(aClass.getContainingFile());
        return true;
      });
      return CachedValueProvider.Result.create(dependencies, dependencies);
    });
  }
}
