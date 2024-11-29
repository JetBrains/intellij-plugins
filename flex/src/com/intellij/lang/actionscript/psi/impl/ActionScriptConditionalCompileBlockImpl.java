// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.psi.impl;

import com.intellij.lang.actionscript.psi.ActionScriptConditionalCompileBlock;
import com.intellij.lang.javascript.psi.JSSourceElement;
import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.lang.javascript.psi.impl.JSBlockStatementImpl;
import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Ulitin
 */
public final class ActionScriptConditionalCompileBlockImpl extends JSStatementImpl implements ActionScriptConditionalCompileBlock {

  public ActionScriptConditionalCompileBlockImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public JSStatement @NotNull [] getStatements() {
    return JSBlockStatementImpl.getStatements(this);
  }

  @Override
  public JSSourceElement @NotNull [] getStatementListItems() {
    return JSBlockStatementImpl.getInnerStatements(this);
  }
}
