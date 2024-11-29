// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.types;

import com.intellij.lang.ASTNode;
import com.intellij.lang.actionscript.psi.impl.JSLocalVariableImpl;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.types.JSVariableElementType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.jetbrains.annotations.NotNull;

public final class ActionScriptLocalVariableElementType extends JSVariableElementType {

  public ActionScriptLocalVariableElementType() {
    super("LOCAL_VARIABLE");
  }

  @Override
  public boolean shouldCreateStub(final ASTNode node) {
    // ActionScriptLocalVariableElementType is stub element type only to not get an assertion from JSLocalVariableImpl.getElementType()
    return false;
  }

  @Override
  public @NotNull JSVariableStub<JSVariable> deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) {
    throw new IllegalStateException("ActionScriptLocalVariableElementType stubs must not be created");
  }

  @Override
  public @NotNull PsiElement construct(@NotNull ASTNode node) {
    return new JSLocalVariableImpl(node);
  }
}