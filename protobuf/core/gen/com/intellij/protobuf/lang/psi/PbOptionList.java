// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbOptionList extends PbBlockBody {

  @NotNull
  List<PbOptionExpression> getOptions();

  @NotNull
  PsiElement getStart();

  @Nullable
  PsiElement getEnd();

}
