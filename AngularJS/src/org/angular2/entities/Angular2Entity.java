// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Angular2Entity extends Angular2Element {

  @NotNull
  String getName();

  @Nullable
  ES6Decorator getDecorator();

  @Nullable
  TypeScriptClass getTypeScriptClass();
}
