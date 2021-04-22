// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbOptionExpression extends PbOptionExpressionBase {

  @Nullable
  PbAggregateValue getAggregateValue();

  @Nullable
  PbIdentifierValue getIdentifierValue();

  @Nullable
  PbNumberValue getNumberValue();

  @NotNull
  PbOptionName getOptionName();

  @Nullable
  PbStringValue getStringValue();

}
