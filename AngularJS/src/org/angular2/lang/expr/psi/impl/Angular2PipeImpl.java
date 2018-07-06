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
import com.intellij.lang.javascript.JSKeywordSets;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2Pipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angular2.lang.expr.parser.Angular2ElementTypes.ARGUMENT_LIST;

public class Angular2PipeImpl extends JSExpressionImpl implements Angular2Pipe {

  public Angular2PipeImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Pipe(this);
    } else {
      super.accept(visitor);
    }
  }

  @Nullable
  @Override
  public JSExpression getExpression() {
    final ASTNode node = findChildByType(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS);
    return node != null ? node.getPsi(JSExpression.class) : null;
  }

  @Nullable
  @Override
  public String getName() {
    final ASTNode node = findChildByType(JSKeywordSets.IDENTIFIER_NAMES);
    return node != null ? node.getText() : null;
  }

  @Nullable
  @Override
  public JSArgumentList getArgumentList() {
    final ASTNode node = findChildByType(ARGUMENT_LIST);
    return node != null ? node.getPsi(JSArgumentList.class) : null;
  }

  @NotNull
  @Override
  public JSExpression[] getArguments() {
    JSArgumentList args = getArgumentList();
    return args != null ? args.getArguments() : JSExpression.EMPTY_ARRAY;
  }

}
