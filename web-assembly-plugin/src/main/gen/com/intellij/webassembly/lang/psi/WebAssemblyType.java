// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface WebAssemblyType extends WebAssemblyNamedElement {

  @Nullable
  WebAssemblyFunctype getFunctype();

  @Nullable
  PsiElement setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

}
