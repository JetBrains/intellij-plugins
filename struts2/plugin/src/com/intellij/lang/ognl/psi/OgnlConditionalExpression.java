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
import org.jetbrains.annotations.NotNull;

/**
 * {@code condition ? then : else}.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlConditionalExpression extends OgnlElement {

  public OgnlConditionalExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  private OgnlExpression findExpression(final int index) {
    final OgnlExpression[] expression = findChildrenByClass(OgnlExpression.class);
    assert index <= expression.length : "no expression at " + index + " '" + getText() + "'";
    return expression[index];
  }

  private OgnlElement findElement(final int index) {
    final OgnlElement[] elements = findChildrenByClass(OgnlElement.class);
    assert index <= elements.length : "no element at " + index + " '" + getText() + "'";
    return elements[index];
  }

  @NotNull
  public OgnlExpression getCondition() {
    return findExpression(0);
  }

  @NotNull
  public OgnlElement getThen() {
    return findElement(1);
  }

  @NotNull
  public OgnlElement getElse() {
    return findElement(2);
  }

}