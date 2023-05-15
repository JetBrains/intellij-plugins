// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface WebAssemblyFunctype extends PsiElement {

  @NotNull
  List<WebAssemblyParam> getParamList();

  @NotNull
  List<WebAssemblyResult> getResultList();

}
