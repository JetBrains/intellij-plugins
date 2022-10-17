// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.lexer;

import com.intellij.plugins.drools.DroolsLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DroolsElementType extends IElementType {

  public DroolsElementType(@NotNull @NonNls String debugName) {
    super(debugName, DroolsLanguage.INSTANCE);
  }
}
