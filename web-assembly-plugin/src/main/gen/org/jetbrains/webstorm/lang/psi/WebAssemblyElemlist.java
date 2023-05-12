// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface WebAssemblyElemlist extends WebAssemblyReferencedElement {

  @NotNull
  List<WebAssemblyIdx> getIdxList();

  @NotNull
  List<WebAssemblyInstr> getInstrList();

  @Nullable
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

}
