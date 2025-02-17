package com.jetbrains.plugins.jade.js;

import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.tree.IElementType;


public class JSInJadeEmbeddedStatementWrapperImpl extends JSStatementImpl {
  public JSInJadeEmbeddedStatementWrapperImpl(IElementType elementType) {
    super(elementType);
  }
}