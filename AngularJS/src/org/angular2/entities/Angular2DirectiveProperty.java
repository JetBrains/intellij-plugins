// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.JSType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Angular2DirectiveProperty extends Angular2Element {

  @NotNull
  String getName();

  @Nullable
  JSType getType();

  boolean isVirtual();
}
