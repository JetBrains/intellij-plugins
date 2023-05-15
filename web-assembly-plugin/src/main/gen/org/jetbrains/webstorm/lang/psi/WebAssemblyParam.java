// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface WebAssemblyParam extends WebAssemblyNamedElement {

  @NotNull
  List<WebAssemblyValtype> getValtypeList();

  @Nullable
  PsiElement setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

}
