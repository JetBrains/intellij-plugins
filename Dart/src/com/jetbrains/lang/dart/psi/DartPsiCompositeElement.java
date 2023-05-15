package com.jetbrains.lang.dart.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.tree.IElementType;

public interface DartPsiCompositeElement extends NavigatablePsiElement {
  IElementType getTokenType();
}
