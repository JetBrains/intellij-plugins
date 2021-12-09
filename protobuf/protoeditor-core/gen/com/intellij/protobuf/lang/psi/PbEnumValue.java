// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbEnumValue extends PbNamedElement, PbOptionOwner {

  @Nullable
  PbNumberValue getNumberValue();

  @Nullable
  PbOptionList getOptionList();

  @Nullable
  PsiElement getNameIdentifier();

}
