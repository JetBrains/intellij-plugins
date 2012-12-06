// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartVarDeclarationList extends DartPsiCompositeElement {

  @NotNull
  DartVarAccessDeclaration getVarAccessDeclaration();

  @NotNull
  List<DartVarDeclarationListPart> getVarDeclarationListPartList();

  @Nullable
  DartVarInit getVarInit();

}
