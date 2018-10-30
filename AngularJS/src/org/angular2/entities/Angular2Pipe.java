// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Angular2Pipe extends Angular2Declaration {

  @NotNull
  Collection<? extends TypeScriptFunction> getTransformMethods();
}
