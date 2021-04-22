// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbExtendBody extends PbBlockBody {

  @NotNull
  List<PbGroupDefinition> getGroupDefinitionList();

  @NotNull
  List<PbSimpleField> getSimpleFieldList();

  @NotNull
  PsiElement getStart();

  @Nullable
  PsiElement getEnd();

}
