// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.intellij.tsr.TslLanguage;

public final class TslTokenType extends IElementType {
  TslTokenType(@NotNull @NonNls String debugName) {
    super(debugName, TslLanguage.INSTANCE);
  }
}
