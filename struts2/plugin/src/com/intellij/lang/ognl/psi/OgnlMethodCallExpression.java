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
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * Method call with optional parameters.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlMethodCallExpression extends OgnlExpressionBase {

  public OgnlMethodCallExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public PsiType getType() {
    return null;
  }

  @NotNull
  public OgnlExpression getMethod() {
    final OgnlExpression expression = getExpression(0);
    assert expression != null : getText();
    return expression;
  }

  @NotNull
  public OgnlExpression getParameter(final int idx) {
    final OgnlExpression expression = getExpression(idx + 1);
    assert expression != null : "no parameter for idx " + idx + ": " + getText();
    return expression;
  }

  public int getParameterCount() {
    return findChildrenByClass(OgnlExpression.class).length - 1;
  }

}