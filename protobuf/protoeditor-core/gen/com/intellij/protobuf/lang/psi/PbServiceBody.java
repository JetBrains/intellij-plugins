// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbServiceBody extends PbBlockBody, PbOptionStatementOwner {

  @NotNull
  List<PbServiceMethod> getServiceMethodList();

  @NotNull
  List<PbServiceStream> getServiceStreamList();

  @NotNull
  List<PbOptionStatement> getOptionStatements();

  @NotNull
  PsiElement getStart();

  @Nullable
  PsiElement getEnd();

}
