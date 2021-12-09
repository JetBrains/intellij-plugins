// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbTextValueList extends ProtoBlockBody {

  @NotNull
  List<PbTextIdentifierValue> getIdentifierValueList();

  @NotNull
  List<PbTextMessageValue> getMessageValueList();

  @NotNull
  List<PbTextNumberValue> getNumberValueList();

  @NotNull
  List<PbTextStringValue> getStringValueList();

  @NotNull
  PsiElement getStart();

  @Nullable
  PsiElement getEnd();

}
