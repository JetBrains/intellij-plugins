// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.psi.impl.source.tree.LazyParseablePsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.psi.DartLazyParseableBlock;
import com.jetbrains.lang.dart.psi.DartStatements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartLazyParseableBlockImpl extends LazyParseablePsiElement implements DartLazyParseableBlock {
  public DartLazyParseableBlockImpl(final @NotNull IElementType type, final @Nullable CharSequence buffer) {
    super(type, buffer);
  }

  @Override
  public IElementType getTokenType() {
    return getElementType();
  }

  @Override
  public @Nullable DartStatements getStatements() {
    return findChildByClass(DartStatements.class);
  }

  @Override
  public String toString() {
    return getTokenType().toString();
  }
}
