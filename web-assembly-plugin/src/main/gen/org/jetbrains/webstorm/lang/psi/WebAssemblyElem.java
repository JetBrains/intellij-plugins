// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface WebAssemblyElem extends WebAssemblyNamedReferencedElement {

  @Nullable
  WebAssemblyElemlist getElemlist();

  @Nullable
  WebAssemblyIdx getIdx();

  @NotNull
  List<WebAssemblyInstr> getInstrList();

  @Nullable
  PsiElement setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

}
