// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbServiceStream extends PbNamedElement, PbOptionStatementOwner {

  @NotNull
  List<PbMessageTypeName> getMessageTypeNameList();

  @Nullable
  PbMethodOptions getMethodOptions();

  @Nullable
  PsiElement getNameIdentifier();

}
