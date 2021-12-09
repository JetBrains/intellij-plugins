// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;

public class PbTextVisitor extends PsiElementVisitor {

  public void visitDomain(@NotNull PbTextDomain o) {
    visitElement(o);
  }

  public void visitExtensionName(@NotNull PbTextExtensionName o) {
    visitElement(o);
    // visitEffectiveReferenceOwner(o);
    // visitProtoSymbolPathContainer(o);
  }

  public void visitField(@NotNull PbTextField o) {
    visitElement(o);
  }

  public void visitFieldName(@NotNull PbTextFieldName o) {
    visitElement(o);
    // visitEffectiveReferenceOwner(o);
  }

  public void visitIdentifierValue(@NotNull PbTextIdentifierValue o) {
    visitLiteral(o);
    // visitProtoIdentifierValue(o);
  }

  public void visitMessageValue(@NotNull PbTextMessageValue o) {
    visitMessage(o);
    // visitProtoBlockBody(o);
  }

  public void visitNumberValue(@NotNull PbTextNumberValue o) {
    visitLiteral(o);
    // visitProtoNumberValue(o);
    // visitProtoBooleanValue(o);
  }

  public void visitStringPart(@NotNull PbTextStringPart o) {
    visitElement(o);
    // visitProtoStringPart(o);
  }

  public void visitStringValue(@NotNull PbTextStringValue o) {
    visitLiteral(o);
    // visitProtoStringValue(o);
  }

  public void visitSymbolPath(@NotNull PbTextSymbolPath o) {
    visitProtoSymbolPath(o);
  }

  public void visitValueList(@NotNull PbTextValueList o) {
    visitProtoBlockBody(o);
  }

  public void visitLiteral(@NotNull PbTextLiteral o) {
    visitElement(o);
  }

  public void visitMessage(@NotNull PbTextMessage o) {
    visitElement(o);
  }

  public void visitProtoBlockBody(@NotNull ProtoBlockBody o) {
    visitElement(o);
  }

  public void visitProtoSymbolPath(@NotNull ProtoSymbolPath o) {
    visitElement(o);
  }

  public void visitElement(@NotNull PbTextElement o) {
    super.visitElement(o);
  }

}
