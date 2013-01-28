/*
 * Copyright 2013 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// Generated from ognl.bnf, do not modify
package com.intellij.lang.ognl.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;

public class OgnlVisitor extends PsiElementVisitor {

  public void visitBinaryExpression(@NotNull OgnlBinaryExpression o) {
    visitExpression(o);
  }

  public void visitConditionalExpression(@NotNull OgnlConditionalExpression o) {
    visitExpression(o);
  }

  public void visitExpression(@NotNull OgnlExpression o) {
    visitPsiCompositeElement(o);
  }

  public void visitIndexedExpression(@NotNull OgnlIndexedExpression o) {
    visitExpression(o);
  }

  public void visitLiteralExpression(@NotNull OgnlLiteralExpression o) {
    visitExpression(o);
  }

  public void visitMethodCallExpression(@NotNull OgnlMethodCallExpression o) {
    visitExpression(o);
  }

  public void visitNewExpression(@NotNull OgnlNewExpression o) {
    visitExpression(o);
  }

  public void visitParenthesizedExpression(@NotNull OgnlParenthesizedExpression o) {
    visitExpression(o);
  }

  public void visitReferenceExpression(@NotNull OgnlReferenceExpression o) {
    visitExpression(o);
  }

  public void visitSequenceExpression(@NotNull OgnlSequenceExpression o) {
    visitExpression(o);
  }

  public void visitUnaryExpression(@NotNull OgnlUnaryExpression o) {
    visitExpression(o);
  }

  public void visitVariableExpression(@NotNull OgnlVariableExpression o) {
    visitExpression(o);
  }

  public void visitPsiCompositeElement(@NotNull OgnlPsiCompositeElement o) {
    visitElement(o);
  }

}
