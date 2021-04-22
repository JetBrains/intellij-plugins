// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbExtensionsStatement extends PbStatement, PbOptionOwner {

  @NotNull
  List<PbExtensionRange> getExtensionRangeList();

  @Nullable
  PbOptionList getOptionList();

}
