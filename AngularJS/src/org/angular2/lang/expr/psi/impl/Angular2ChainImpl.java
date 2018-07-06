// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2Chain;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.jetbrains.annotations.NotNull;

public class Angular2ChainImpl extends JSStatementImpl implements Angular2Chain {

  public Angular2ChainImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Chain(this);
    } else {
      super.accept(visitor);
    }
  }

  @Override
  @NotNull
  public JSExpression[] getExpressions() {
    final ASTNode[] nodes = getChildren(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS);
    if (nodes.length == 0) return JSExpression.EMPTY_ARRAY;
    final JSExpression[] exprs = new JSExpression[nodes.length];
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = nodes[i].getPsi(JSExpression.class);
    }
    return exprs;
  }

}
