// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.StubBasedPsiElement;
import org.intellij.prisma.lang.psi.stubs.PrismaKeyValueStub;
import org.intellij.prisma.lang.psi.stubs.PrismaViewDeclarationStub;
import org.intellij.prisma.lang.psi.stubs.PrismaEnumValueDeclarationStub;
import org.intellij.prisma.lang.psi.stubs.PrismaFieldDeclarationStub;
import org.intellij.prisma.lang.psi.stubs.PrismaGeneratorDeclarationStub;
import org.intellij.prisma.lang.psi.stubs.PrismaDatasourceDeclarationStub;
import org.intellij.prisma.lang.psi.stubs.PrismaTypeAliasStub;
import org.intellij.prisma.lang.psi.stubs.PrismaEnumDeclarationStub;
import org.intellij.prisma.lang.psi.stubs.PrismaModelDeclarationStub;
import org.intellij.prisma.lang.psi.stubs.PrismaTypeDeclarationStub;

public class PrismaVisitor extends PsiElementVisitor {

  public void visitArgument(@NotNull PrismaArgument o) {
    visitElement(o);
  }

  public void visitArgumentsList(@NotNull PrismaArgumentsList o) {
    visitElement(o);
  }

  public void visitArrayExpression(@NotNull PrismaArrayExpression o) {
    visitExpression(o);
  }

  public void visitBlockAttribute(@NotNull PrismaBlockAttribute o) {
    visitArgumentsOwner(o);
  }

  public void visitDatasourceDeclaration(@NotNull PrismaDatasourceDeclaration o) {
    visitStubBasedPsiElement(o);
    // visitDeclaration(o);
    // visitKeyValueDeclaration(o);
  }

  public void visitEnumDeclaration(@NotNull PrismaEnumDeclaration o) {
    visitStubBasedPsiElement(o);
    // visitDeclaration(o);
    // visitEntityDeclaration(o);
  }

  public void visitEnumDeclarationBlock(@NotNull PrismaEnumDeclarationBlock o) {
    visitBlock(o);
  }

  public void visitEnumValueDeclaration(@NotNull PrismaEnumValueDeclaration o) {
    visitStubBasedPsiElement(o);
    // visitMemberDeclaration(o);
    // visitFieldAttributeOwner(o);
  }

  public void visitExpression(@NotNull PrismaExpression o) {
    visitElement(o);
  }

  public void visitFieldAttribute(@NotNull PrismaFieldAttribute o) {
    visitArgumentsOwner(o);
  }

  public void visitFieldDeclaration(@NotNull PrismaFieldDeclaration o) {
    visitStubBasedPsiElement(o);
    // visitMemberDeclaration(o);
    // visitTypeOwner(o);
    // visitFieldAttributeOwner(o);
  }

  public void visitFieldDeclarationBlock(@NotNull PrismaFieldDeclarationBlock o) {
    visitBlock(o);
  }

  public void visitFieldType(@NotNull PrismaFieldType o) {
    visitTypeSignature(o);
  }

  public void visitFunctionCall(@NotNull PrismaFunctionCall o) {
    visitExpression(o);
    // visitArgumentsOwner(o);
  }

  public void visitGeneratorDeclaration(@NotNull PrismaGeneratorDeclaration o) {
    visitStubBasedPsiElement(o);
    // visitDeclaration(o);
    // visitKeyValueDeclaration(o);
  }

  public void visitKeyValue(@NotNull PrismaKeyValue o) {
    visitStubBasedPsiElement(o);
    // visitMemberDeclaration(o);
  }

  public void visitKeyValueBlock(@NotNull PrismaKeyValueBlock o) {
    visitBlock(o);
  }

  public void visitLegacyListType(@NotNull PrismaLegacyListType o) {
    visitFieldType(o);
  }

  public void visitLegacyRequiredType(@NotNull PrismaLegacyRequiredType o) {
    visitFieldType(o);
  }

  public void visitListType(@NotNull PrismaListType o) {
    visitFieldType(o);
  }

  public void visitLiteralExpression(@NotNull PrismaLiteralExpression o) {
    visitExpression(o);
  }

  public void visitModelDeclaration(@NotNull PrismaModelDeclaration o) {
    visitStubBasedPsiElement(o);
    // visitDeclaration(o);
    // visitEntityDeclaration(o);
    // visitTableEntityDeclaration(o);
  }

  public void visitNamedArgument(@NotNull PrismaNamedArgument o) {
    visitArgument(o);
    // visitReferenceElement(o);
  }

  public void visitOptionalType(@NotNull PrismaOptionalType o) {
    visitFieldType(o);
  }

  public void visitPathExpression(@NotNull PrismaPathExpression o) {
    visitExpression(o);
    // visitQualifiedReferenceElement(o);
  }

  public void visitSingleType(@NotNull PrismaSingleType o) {
    visitFieldType(o);
  }

  public void visitTypeAlias(@NotNull PrismaTypeAlias o) {
    visitStubBasedPsiElement(o);
    // visitDeclaration(o);
    // visitEntityDeclaration(o);
  }

  public void visitTypeDeclaration(@NotNull PrismaTypeDeclaration o) {
    visitStubBasedPsiElement(o);
    // visitDeclaration(o);
    // visitEntityDeclaration(o);
    // visitTableEntityDeclaration(o);
  }

  public void visitTypeReference(@NotNull PrismaTypeReference o) {
    visitReferenceElement(o);
  }

  public void visitUnsupportedOptionalListType(@NotNull PrismaUnsupportedOptionalListType o) {
    visitFieldType(o);
  }

  public void visitUnsupportedType(@NotNull PrismaUnsupportedType o) {
    visitElement(o);
  }

  public void visitValueArgument(@NotNull PrismaValueArgument o) {
    visitArgument(o);
  }

  public void visitViewDeclaration(@NotNull PrismaViewDeclaration o) {
    visitStubBasedPsiElement(o);
    // visitDeclaration(o);
    // visitEntityDeclaration(o);
    // visitTableEntityDeclaration(o);
  }

  public void visitStubBasedPsiElement(@NotNull StubBasedPsiElement o) {
    visitElement(o);
  }

  public void visitArgumentsOwner(@NotNull PrismaArgumentsOwner o) {
    visitElement(o);
  }

  public void visitBlock(@NotNull PrismaBlock o) {
    visitElement(o);
  }

  public void visitReferenceElement(@NotNull PrismaReferenceElement o) {
    visitElement(o);
  }

  public void visitTypeSignature(@NotNull PrismaTypeSignature o) {
    visitElement(o);
  }

  public void visitElement(@NotNull PrismaElement o) {
    super.visitElement(o);
  }

}
