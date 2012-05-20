/*
 * Copyright 2012 The authors
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
 * {@code new Type(args)} or {@code new Type[] { e1, e2, ..} }
 *
 * @author Yann C&eacute;bron
 */
public class OgnlNewExpression extends OgnlExpressionBase {

  public OgnlNewExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public PsiType getType() {
    return getNewType().getType();
  }

  @NotNull
  public OgnlExpression getNewType() {
    final OgnlExpression expression = getExpression(0);
    assert expression != null : "no expression: " + getText();
    return expression;
  }

}