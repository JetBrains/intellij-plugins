// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbReservedRange extends PbReservedRangeBase {

  @NotNull
  List<PbNumberValue> getNumberValueList();

  @NotNull
  PbNumberValue getFromValue();

  @Nullable
  PbNumberValue getToValue();

}
