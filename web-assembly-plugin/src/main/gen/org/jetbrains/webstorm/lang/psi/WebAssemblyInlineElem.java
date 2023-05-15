// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface WebAssemblyInlineElem extends PsiElement {

  @Nullable
  WebAssemblyElemlist getElemlist();

  @NotNull
  List<WebAssemblyInstr> getInstrList();

}
