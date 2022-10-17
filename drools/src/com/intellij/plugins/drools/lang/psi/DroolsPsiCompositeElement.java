package com.intellij.plugins.drools.lang.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.tree.IElementType;

public interface DroolsPsiCompositeElement extends NavigatablePsiElement {
  IElementType getTokenType();
}
