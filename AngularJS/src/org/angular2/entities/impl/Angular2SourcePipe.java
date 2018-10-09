// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.impl;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.psi.util.CachedValueProvider;
import org.angular2.entities.Angular2Pipe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class Angular2SourcePipe extends Angular2SourceDeclaration implements Angular2Pipe {

  private final String myName;

  public Angular2SourcePipe(@NotNull ES6Decorator source, @NotNull String name) {
    super(source);
    this.myName = name;
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @NotNull
  @Override
  public Collection<? extends TypeScriptFunction> getTransformMethods() {
    //noinspection unchecked,RedundantCast
    return getCachedValue(() -> CachedValueProvider.Result.create(
      (Collection<? extends TypeScriptFunction>)(Collection)TypeScriptTypeParser
        .buildTypeFromClass(getTypeScriptClass(), false)
        .getProperties()
        .stream()
        .filter(prop -> "transform".equals(prop.getMemberName())
                        && prop.getMemberSource()
                          .getSingleElement() instanceof TypeScriptFunction)
        .findFirst()
        .map(sig -> sig.getMemberSource()
          .getAllSourceElements()
          .stream()
          .filter(fun -> fun instanceof TypeScriptFunction && !(fun instanceof TypeScriptFunctionSignature))
          .collect(Collectors.toList()))
        .map(Collections::unmodifiableCollection)
        .orElseGet(Collections::emptyList), getClassModificationDependencies())
    );
  }

}
