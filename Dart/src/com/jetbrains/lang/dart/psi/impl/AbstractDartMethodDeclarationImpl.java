// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.jetbrains.lang.dart.psi.DartComponentName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDartMethodDeclarationImpl extends AbstractDartComponentImpl {
  public AbstractDartMethodDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable DartComponentName getComponentName() {
    return findChildByClass(DartComponentName.class);
  }
}
