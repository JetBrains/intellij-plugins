// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.jhipster.JdlLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class JdlElementType extends IElementType {
  JdlElementType(@NotNull @NonNls String debugName) {
    super(debugName, JdlLanguage.INSTANCE);
  }
}
