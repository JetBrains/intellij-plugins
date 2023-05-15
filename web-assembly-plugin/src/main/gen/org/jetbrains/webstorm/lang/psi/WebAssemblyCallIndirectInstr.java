// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface WebAssemblyCallIndirectInstr extends WebAssemblyReferencedElement {

  @Nullable
  WebAssemblyIdx getIdx();

  @Nullable
  WebAssemblyTypeuse getTypeuse();

  @Nullable
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

}
