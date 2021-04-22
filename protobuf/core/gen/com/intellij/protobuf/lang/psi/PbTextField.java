// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbTextField extends PbTextFieldBase {

  @NotNull
  PbTextFieldName getFieldName();

  @Nullable
  PbTextIdentifierValue getIdentifierValue();

  @Nullable
  PbTextMessageValue getMessageValue();

  @Nullable
  PbTextNumberValue getNumberValue();

  @Nullable
  PbTextStringValue getStringValue();

  @Nullable
  PbTextValueList getValueList();

}
