/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * Unary expression.
 * <p/>
 * Currently only operation {@link com.intellij.lang.ognl.psi.OgnlTokenTypes#MINUS}.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlUnaryExpression extends OgnlExpressionBase {

  public OgnlUnaryExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  public OgnlTokenType getOperation() {
    final PsiElement firstChild = getFirstChild();
    assert firstChild != null : "no operation: " + getText();
    return (OgnlTokenType) firstChild.getNode().getElementType();
  }

  @NotNull
  public OgnlExpression getOperand() {
    final OgnlExpression expression = getExpression(0);
    assert expression != null : "no operand: " + getText();
    return expression;
  }

  public PsiType getType() {
    return getExpressionType(0);
  }

}