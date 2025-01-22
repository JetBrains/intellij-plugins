// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import org.intellij.terraform.hcl.psi.common.Identifier;
import org.intellij.terraform.hcl.psi.common.UnaryExpression;
import org.intellij.terraform.hcl.psi.common.LiteralExpression;
import org.intellij.terraform.hcl.psi.common.MethodCallExpression;
import org.intellij.terraform.hcl.psi.common.BinaryExpression;
import org.intellij.terraform.hcl.psi.common.CollectionExpression;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.intellij.terraform.hcl.psi.common.ParenthesizedExpression;
import org.intellij.terraform.hcl.psi.common.IndexSelectExpression;
import org.intellij.terraform.hcl.psi.common.ConditionalExpression;
import org.intellij.terraform.hcl.psi.common.SelectExpression;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.NavigatablePsiElement;
import org.intellij.terraform.hcl.psi.common.BaseExpression;
import com.intellij.psi.PsiElement;
import org.intellij.terraform.hcl.psi.common.ParameterList;

public class HCLElementVisitor extends PsiElementVisitor {

  public void visitBinaryAdditionExpression(@NotNull HCLBinaryAdditionExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryAndExpression(@NotNull HCLBinaryAndExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryEqualityExpression(@NotNull HCLBinaryEqualityExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryExpression(@NotNull HCLBinaryExpression o) {
    visitExpression(o);
    // visitBinaryExpression(o);
  }

  public void visitBinaryMultiplyExpression(@NotNull HCLBinaryMultiplyExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryOrExpression(@NotNull HCLBinaryOrExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryRelationalExpression(@NotNull HCLBinaryRelationalExpression o) {
    visitBinaryExpression(o);
  }

  public void visitCollectionValue(@NotNull HCLCollectionValue o) {
    visitExpression(o);
  }

  public void visitConditionalExpression(@NotNull HCLConditionalExpression o) {
    visitExpression(o);
    // visitConditionalExpression(o);
  }

  public void visitDefinedMethodExpression(@NotNull HCLDefinedMethodExpression o) {
    visitExpression(o);
  }

  public void visitExpression(@NotNull HCLExpression o) {
    visitBaseExpression(o);
  }

  public void visitForArrayExpression(@NotNull HCLForArrayExpression o) {
    visitForExpression(o);
  }

  public void visitForCondition(@NotNull HCLForCondition o) {
    visitElement(o);
  }

  public void visitForExpression(@NotNull HCLForExpression o) {
    visitExpression(o);
  }

  public void visitForIntro(@NotNull HCLForIntro o) {
    visitElement(o);
  }

  public void visitForObjectExpression(@NotNull HCLForObjectExpression o) {
    visitForExpression(o);
  }

  public void visitIndexSelectExpression(@NotNull HCLIndexSelectExpression o) {
    visitSelectExpression(o);
    // visitIndexSelectExpression(o);
  }

  public void visitMethodCallExpression(@NotNull HCLMethodCallExpression o) {
    visitExpression(o);
    // visitMethodCallExpression(o);
  }

  public void visitParameterList(@NotNull HCLParameterList o) {
    visitPsiElement(o);
    // visitParameterList(o);
  }

  public void visitParenthesizedExpression(@NotNull HCLParenthesizedExpression o) {
    visitExpression(o);
    // visitParenthesizedExpression(o);
  }

  public void visitSelectExpression(@NotNull HCLSelectExpression o) {
    visitExpression(o);
    // visitSelectExpression(o);
  }

  public void visitUnaryExpression(@NotNull HCLUnaryExpression o) {
    visitExpression(o);
    // visitUnaryExpression(o);
  }

  public void visitVariable(@NotNull HCLVariable o) {
    visitExpression(o);
    // visitIdentifier(o);
  }

  public void visitArray(@NotNull HCLArray o) {
    visitContainer(o);
    // visitCollectionExpression(o);
  }

  public void visitBlock(@NotNull HCLBlock o) {
    visitElement(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitBlockObject(@NotNull HCLBlockObject o) {
    visitObject(o);
  }

  public void visitBooleanLiteral(@NotNull HCLBooleanLiteral o) {
    visitLiteral(o);
  }

  public void visitContainer(@NotNull HCLContainer o) {
    visitValue(o);
  }

  public void visitHeredocContent(@NotNull HCLHeredocContent o) {
    visitElement(o);
  }

  public void visitHeredocLiteral(@NotNull HCLHeredocLiteral o) {
    visitLiteral(o);
  }

  public void visitHeredocMarker(@NotNull HCLHeredocMarker o) {
    visitElement(o);
  }

  public void visitIdentifier(@NotNull HCLIdentifier o) {
    visitValue(o);
    // visitIdentifier(o);
  }

  public void visitLiteral(@NotNull HCLLiteral o) {
    visitValue(o);
    // visitLiteralExpression(o);
  }

  public void visitNullLiteral(@NotNull HCLNullLiteral o) {
    visitLiteral(o);
  }

  public void visitNumberLiteral(@NotNull HCLNumberLiteral o) {
    visitLiteral(o);
  }

  public void visitObject(@NotNull HCLObject o) {
    visitContainer(o);
    // visitCollectionExpression(o);
  }

  public void visitProperty(@NotNull HCLProperty o) {
    visitElement(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitStringLiteral(@NotNull HCLStringLiteral o) {
    visitLiteral(o);
    // visitPsiLanguageInjectionHost(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitValue(@NotNull HCLValue o) {
    visitExpression(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

  public void visitBaseExpression(@NotNull BaseExpression o) {
    visitElement(o);
  }

  public void visitElement(@NotNull HCLElement o) {
    super.visitElement(o);
  }

}
