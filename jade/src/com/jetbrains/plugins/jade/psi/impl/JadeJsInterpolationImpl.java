package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;

public class JadeJsInterpolationImpl extends CompositePsiElement {

  public JadeJsInterpolationImpl() {
    super(JadeElementTypes.JS_INTERPOLATION);
  }

}
