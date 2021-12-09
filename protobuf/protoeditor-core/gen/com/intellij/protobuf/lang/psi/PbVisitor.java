// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;

public class PbVisitor extends PsiElementVisitor {

  public void visitAggregateValue(@NotNull PbAggregateValue o) {
    visitLiteral(o);
    // visitBlockBody(o);
    // visitTextRootMessage(o);
  }

  public void visitEnumBody(@NotNull PbEnumBody o) {
    visitBlockBody(o);
    // visitOptionStatementOwner(o);
  }

  public void visitEnumDefinition(@NotNull PbEnumDefinition o) {
    visitDefinition(o);
    // visitNamedTypeElement(o);
  }

  public void visitEnumReservedRange(@NotNull PbEnumReservedRange o) {
    visitElement(o);
  }

  public void visitEnumReservedStatement(@NotNull PbEnumReservedStatement o) {
    visitStatement(o);
  }

  public void visitEnumValue(@NotNull PbEnumValue o) {
    visitNamedElement(o);
    // visitOptionOwner(o);
  }

  public void visitExtendBody(@NotNull PbExtendBody o) {
    visitBlockBody(o);
  }

  public void visitExtendDefinition(@NotNull PbExtendDefinition o) {
    visitDefinition(o);
  }

  public void visitExtensionName(@NotNull PbExtensionName o) {
    visitQualifiedReference(o);
    // visitEffectiveReferenceOwner(o);
    // visitProtoSymbolPathContainer(o);
  }

  public void visitExtensionRange(@NotNull PbExtensionRange o) {
    visitElement(o);
  }

  public void visitExtensionsStatement(@NotNull PbExtensionsStatement o) {
    visitStatement(o);
    // visitOptionOwner(o);
  }

  public void visitFieldLabel(@NotNull PbFieldLabel o) {
    visitElement(o);
  }

  public void visitGroupDefinition(@NotNull PbGroupDefinition o) {
    visitDefinition(o);
    // visitMessageType(o);
    // visitSymbolContributor(o);
  }

  public void visitGroupOptionContainer(@NotNull PbGroupOptionContainer o) {
    visitElement(o);
  }

  public void visitIdentifierValue(@NotNull PbIdentifierValue o) {
    visitLiteral(o);
    // visitProtoIdentifierValue(o);
  }

  public void visitImportName(@NotNull PbImportName o) {
    visitElement(o);
  }

  public void visitImportStatement(@NotNull PbImportStatement o) {
    visitElement(o);
    // visitStatement(o);
  }

  public void visitMapField(@NotNull PbMapField o) {
    visitField(o);
    // visitSymbolContributor(o);
  }

  public void visitMessageBody(@NotNull PbMessageBody o) {
    visitBlockBody(o);
    // visitOptionStatementOwner(o);
  }

  public void visitMessageDefinition(@NotNull PbMessageDefinition o) {
    visitDefinition(o);
    // visitMessageType(o);
  }

  public void visitMessageTypeName(@NotNull PbMessageTypeName o) {
    visitTypeName(o);
  }

  public void visitMethodOptions(@NotNull PbMethodOptions o) {
    visitBlockBody(o);
  }

  public void visitNumberValue(@NotNull PbNumberValue o) {
    visitLiteral(o);
    // visitProtoNumberValue(o);
  }

  public void visitOneofBody(@NotNull PbOneofBody o) {
    visitBlockBody(o);
    // visitOptionStatementOwner(o);
  }

  public void visitOneofDefinition(@NotNull PbOneofDefinition o) {
    visitDefinition(o);
    // visitNamedElement(o);
  }

  public void visitOptionExpression(@NotNull PbOptionExpression o) {
    visitElement(o);
  }

  public void visitOptionList(@NotNull PbOptionList o) {
    visitBlockBody(o);
  }

  public void visitOptionName(@NotNull PbOptionName o) {
    visitElement(o);
    // visitEffectiveReferenceOwner(o);
  }

  public void visitOptionStatement(@NotNull PbOptionStatement o) {
    visitStatement(o);
  }

  public void visitPackageName(@NotNull PbPackageName o) {
    visitElement(o);
    // visitSymbol(o);
    // visitSymbolOwner(o);
  }

  public void visitPackageStatement(@NotNull PbPackageStatement o) {
    visitStatement(o);
  }

  public void visitReservedRange(@NotNull PbReservedRange o) {
    visitElement(o);
  }

  public void visitReservedStatement(@NotNull PbReservedStatement o) {
    visitStatement(o);
  }

  public void visitServiceBody(@NotNull PbServiceBody o) {
    visitBlockBody(o);
    // visitOptionStatementOwner(o);
  }

  public void visitServiceDefinition(@NotNull PbServiceDefinition o) {
    visitDefinition(o);
    // visitNamedElement(o);
    // visitSymbolOwner(o);
  }

  public void visitServiceMethod(@NotNull PbServiceMethod o) {
    visitNamedElement(o);
    // visitOptionStatementOwner(o);
  }

  public void visitServiceMethodType(@NotNull PbServiceMethodType o) {
    visitElement(o);
  }

  public void visitServiceStream(@NotNull PbServiceStream o) {
    visitNamedElement(o);
    // visitOptionStatementOwner(o);
  }

  public void visitSimpleField(@NotNull PbSimpleField o) {
    visitField(o);
  }

  public void visitStringPart(@NotNull PbStringPart o) {
    visitElement(o);
    // visitProtoStringPart(o);
  }

  public void visitStringValue(@NotNull PbStringValue o) {
    visitLiteral(o);
    // visitProtoStringValue(o);
  }

  public void visitSymbolPath(@NotNull PbSymbolPath o) {
    visitProtoSymbolPath(o);
  }

  public void visitSyntaxStatement(@NotNull PbSyntaxStatement o) {
    visitElement(o);
    // visitStatement(o);
  }

  public void visitTypeName(@NotNull PbTypeName o) {
    visitElement(o);
    // visitQualifiedReference(o);
    // visitEffectiveReferenceOwner(o);
    // visitProtoSymbolPathContainer(o);
  }

  public void visitBlockBody(@NotNull PbBlockBody o) {
    visitElement(o);
  }

  public void visitDefinition(@NotNull PbDefinition o) {
    visitElement(o);
  }

  public void visitField(@NotNull PbField o) {
    visitElement(o);
  }

  public void visitLiteral(@NotNull PbLiteral o) {
    visitElement(o);
  }

  public void visitNamedElement(@NotNull PbNamedElement o) {
    visitElement(o);
  }

  public void visitQualifiedReference(@NotNull PbQualifiedReference o) {
    visitElement(o);
  }

  public void visitStatement(@NotNull PbStatement o) {
    visitElement(o);
  }

  public void visitProtoSymbolPath(@NotNull ProtoSymbolPath o) {
    visitElement(o);
  }

  public void visitElement(@NotNull PbElement o) {
    super.visitElement(o);
  }

}
