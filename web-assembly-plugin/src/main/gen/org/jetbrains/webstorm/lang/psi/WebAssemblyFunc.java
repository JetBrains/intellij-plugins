// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface WebAssemblyFunc extends WebAssemblyNamedElement {

  @NotNull
  List<WebAssemblyInlineExport> getInlineExportList();

  @Nullable
  WebAssemblyInlineImport getInlineImport();

  @NotNull
  List<WebAssemblyInstr> getInstrList();

  @NotNull
  List<WebAssemblyLocal> getLocalList();

  @Nullable
  WebAssemblyTypeuse getTypeuse();

  @Nullable
  PsiElement setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

}
