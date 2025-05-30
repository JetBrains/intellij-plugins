// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.types;

import com.intellij.lang.ASTNode;
import com.intellij.lang.actionscript.psi.impl.JSLocalVariableImpl;
import com.intellij.lang.javascript.types.JSVariableElementType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public final class ActionScriptLocalVariableElementType extends JSVariableElementType {

  public ActionScriptLocalVariableElementType() {
    super("LOCAL_VARIABLE");
  }

  @Override
  public @NotNull PsiElement construct(@NotNull ASTNode node) {
    return new JSLocalVariableImpl(node);
  }
}