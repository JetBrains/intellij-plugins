// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class Angular2EntityUtils {

  @NotNull
  public static Collection<? extends TypeScriptFunction> getPipeTransformMethods(@NotNull TypeScriptClass cls) {
    //noinspection RedundantCast,unchecked
    return (Collection<? extends TypeScriptFunction>)(Collection)TypeScriptTypeParser
      .buildTypeFromClass(cls, false)
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
      .orElseGet(Collections::emptyList);
  }
}
