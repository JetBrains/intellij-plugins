package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeJSStatementImpl extends CompositePsiElement {
  public JadeJSStatementImpl() {
    super(JadeElementTypes.JS_STATEMENT);
  }

  public boolean hasJadeBlock() {
    return ((JadeJSCodeLineImpl)getFirstChild()).shouldIndent();
  }
}
