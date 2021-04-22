// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbSimpleField extends PbField {

  @Nullable
  PbOptionList getOptionList();

  @NotNull
  PbTypeName getTypeName();

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  PbNumberValue getFieldNumber();

  @Nullable
  PbFieldLabel getDeclaredLabel();

}
