// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbMapField extends PbField, PbSymbolContributor {

  @Nullable
  PbOptionList getOptionList();

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  PbNumberValue getFieldNumber();

  @Nullable
  PbTypeName getKeyType();

  @Nullable
  PbTypeName getValueType();

  @Nullable
  PbFieldLabel getDeclaredLabel();

}
