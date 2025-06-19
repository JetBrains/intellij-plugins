// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbEnumBody extends PbBlockBody, PbOptionStatementOwner {

  @NotNull
  List<PbEnumReservedStatement> getEnumReservedStatementList();

  @NotNull
  List<PbEnumValue> getEnumValueList();

  @NotNull
  List<PbOptionStatement> getOptionStatements();

  @NotNull
  PsiElement getStart();

  @Nullable
  PsiElement getEnd();

}
