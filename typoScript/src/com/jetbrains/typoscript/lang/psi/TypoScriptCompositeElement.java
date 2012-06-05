package com.jetbrains.typoscript.lang.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.tree.IElementType;


public interface TypoScriptCompositeElement extends NavigatablePsiElement {
  IElementType getTokenType();
}
