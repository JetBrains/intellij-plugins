// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PbMessageBody extends PbBlockBody, PbOptionStatementOwner {

  @NotNull
  List<PbEnumDefinition> getEnumDefinitionList();

  @NotNull
  List<PbExtendDefinition> getExtendDefinitionList();

  @NotNull
  List<PbExtensionsStatement> getExtensionsStatementList();

  @NotNull
  List<PbGroupDefinition> getGroupDefinitionList();

  @NotNull
  List<PbMapField> getMapFieldList();

  @NotNull
  List<PbMessageDefinition> getMessageDefinitionList();

  @NotNull
  List<PbOneofDefinition> getOneofDefinitionList();

  @NotNull
  List<PbReservedStatement> getReservedStatementList();

  @NotNull
  List<PbSimpleField> getSimpleFieldList();

  @NotNull
  List<PbOptionStatement> getOptionStatements();

  @NotNull
  PsiElement getStart();

  @Nullable
  PsiElement getEnd();

}
