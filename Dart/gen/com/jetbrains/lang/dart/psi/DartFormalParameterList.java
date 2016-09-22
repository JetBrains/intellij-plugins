// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartFormalParameterList extends DartPsiCompositeElement {

  @NotNull
  List<DartNormalFormalParameter> getNormalFormalParameterList();

  @Nullable
  DartOptionalFormalParameters getOptionalFormalParameters();

}
