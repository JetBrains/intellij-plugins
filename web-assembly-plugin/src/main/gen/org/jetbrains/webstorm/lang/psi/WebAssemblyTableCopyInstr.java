// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface WebAssemblyTableCopyInstr extends WebAssemblyReferencedElement {

  @NotNull
  List<WebAssemblyIdx> getIdxList();

  @Nullable
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

}
