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
import com.intellij.lang.javascript.JSKeywordSets;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2Quote;
import org.jetbrains.annotations.NotNull;

public class Angular2QuoteImpl extends JSStatementImpl implements Angular2Quote {

  public Angular2QuoteImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  @NotNull
  public String getName() {
    final ASTNode node = findChildByType(JSKeywordSets.IDENTIFIER_NAMES);
    return node != null ? node.getText() : "";
  }

  @Override
  @NotNull
  public String getContents() {
    final ASTNode colon = findChildByType(JSTokenTypes.COLON);
    return colon != null ? this.getText().substring(colon.getStartOffset() - getStartOffset() + 1) : "";
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Quote(this);
    } else {
      super.accept(visitor);
    }
  }
}
