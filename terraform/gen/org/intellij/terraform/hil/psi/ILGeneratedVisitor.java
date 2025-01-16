// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import org.intellij.terraform.hcl.psi.common.LiteralExpression;
import org.intellij.terraform.hcl.psi.common.BaseExpression;
import org.intellij.terraform.hcl.psi.common.UnaryExpression;
import org.intellij.terraform.hcl.psi.common.ConditionalExpression;
import org.intellij.terraform.hcl.psi.common.CollectionExpression;
import org.intellij.terraform.hcl.psi.common.ParenthesizedExpression;
import org.intellij.terraform.hcl.psi.common.Identifier;
import org.intellij.terraform.hcl.psi.common.ParameterList;
import org.intellij.terraform.hcl.psi.common.BinaryExpression;
import org.intellij.terraform.hcl.psi.common.SelectExpression;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.intellij.terraform.hcl.psi.common.MethodCallExpression;
import org.intellij.terraform.hcl.psi.common.IndexSelectExpression;

public class ILGeneratedVisitor extends PsiElementVisitor {

  public void visitBadTag(@NotNull BadTag o) {
    visitILExpression(o);
  }

  public void visitElseCondition(@NotNull ElseCondition o) {
    visitILExpression(o);
  }

  public void visitEndFor(@NotNull EndFor o) {
    visitILExpression(o);
  }

  public void visitEndIf(@NotNull EndIf o) {
    visitILExpression(o);
  }

  public void visitForCondition(@NotNull ForCondition o) {
    visitILExpression(o);
  }

  public void visitForVariable(@NotNull ForVariable o) {
    visitILExpression(o);
  }

  public void visitILArray(@NotNull ILArray o) {
    visitCollectionExpression(o);
  }

  public void visitILBinaryAdditionExpression(@NotNull ILBinaryAdditionExpression o) {
    visitILBinaryExpression(o);
  }

  public void visitILBinaryAndExpression(@NotNull ILBinaryAndExpression o) {
    visitILBinaryExpression(o);
  }

  public void visitILBinaryEqualityExpression(@NotNull ILBinaryEqualityExpression o) {
    visitILBinaryExpression(o);
  }

  public void visitILBinaryExpression(@NotNull ILBinaryExpression o) {
    visitILExpression(o);
    // visitBinaryExpression(o);
  }

  public void visitILBinaryMultiplyExpression(@NotNull ILBinaryMultiplyExpression o) {
    visitILBinaryExpression(o);
  }

  public void visitILBinaryOrExpression(@NotNull ILBinaryOrExpression o) {
    visitILBinaryExpression(o);
  }

  public void visitILBinaryRelationalExpression(@NotNull ILBinaryRelationalExpression o) {
    visitILBinaryExpression(o);
  }

  public void visitILCollectionValue(@NotNull ILCollectionValue o) {
    visitILExpression(o);
  }

  public void visitILConditionalExpression(@NotNull ILConditionalExpression o) {
    visitILExpression(o);
    // visitConditionalExpression(o);
  }

  public void visitILDefinedMethodExpression(@NotNull ILDefinedMethodExpression o) {
    visitILExpression(o);
  }

  public void visitILExpression(@NotNull ILExpression o) {
    visitBaseExpression(o);
  }

  public void visitILExpressionHolder(@NotNull ILExpressionHolder o) {
    visitILExpression(o);
    // visitParenthesizedExpression(o);
  }

  public void visitILIndexSelectExpression(@NotNull ILIndexSelectExpression o) {
    visitILSelectExpression(o);
    // visitIndexSelectExpression(o);
  }

  public void visitILLiteralExpression(@NotNull ILLiteralExpression o) {
    visitILExpression(o);
    // visitLiteralExpression(o);
  }

  public void visitILMethodCallExpression(@NotNull ILMethodCallExpression o) {
    visitILExpression(o);
    // visitMethodCallExpression(o);
  }

  public void visitILObject(@NotNull ILObject o) {
    visitCollectionExpression(o);
  }

  public void visitILParameterList(@NotNull ILParameterList o) {
    visitParameterList(o);
  }

  public void visitILParenthesizedExpression(@NotNull ILParenthesizedExpression o) {
    visitILExpression(o);
    // visitParenthesizedExpression(o);
  }

  public void visitILProperty(@NotNull ILProperty o) {
    visitILExpression(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitILSelectExpression(@NotNull ILSelectExpression o) {
    visitILExpression(o);
    // visitSelectExpression(o);
  }

  public void visitILSimpleExpression(@NotNull ILSimpleExpression o) {
    visitILExpression(o);
  }

  public void visitILTemplateBlockBody(@NotNull ILTemplateBlockBody o) {
    visitILExpression(o);
  }

  public void visitILTemplateForBlockExpression(@NotNull ILTemplateForBlockExpression o) {
    visitILExpression(o);
  }

  public void visitILTemplateHolder(@NotNull ILTemplateHolder o) {
    visitILExpression(o);
  }

  public void visitILTemplateIfBlockExpression(@NotNull ILTemplateIfBlockExpression o) {
    visitILExpression(o);
  }

  public void visitILUnaryExpression(@NotNull ILUnaryExpression o) {
    visitILExpression(o);
    // visitUnaryExpression(o);
  }

  public void visitILVariable(@NotNull ILVariable o) {
    visitILExpression(o);
    // visitIdentifier(o);
  }

  public void visitIfCondition(@NotNull IfCondition o) {
    visitILExpression(o);
  }

  public void visitBaseExpression(@NotNull BaseExpression o) {
    visitElement(o);
  }

  public void visitCollectionExpression(@NotNull CollectionExpression o) {
    visitElement(o);
  }

  public void visitParameterList(@NotNull ParameterList o) {
    visitElement(o);
  }

}
