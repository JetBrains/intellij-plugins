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
package org.angular2.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSCommaExpression;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSParenthesizedExpression;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Dennis.Ushakov
 */
public class Angular2RepeatExpression extends JSExpressionImpl {
  public Angular2RepeatExpression(IElementType elementType) {
    super(elementType);
  }

  public Collection<JSDefinitionExpression> getDefinitions() {
    final PsiElement firstChild = getFirstChild();
    if (firstChild instanceof JSDefinitionExpression) {
      return Collections.singletonList((JSDefinitionExpression)firstChild);
    } else if (firstChild instanceof JSParenthesizedExpression) {
      final PsiElement commaExpression = PsiTreeUtil.findChildOfType(firstChild, JSCommaExpression.class);
      if (commaExpression != null) {
        return PsiTreeUtil.findChildrenOfType(commaExpression, JSDefinitionExpression.class);
      }
    }
    return Collections.emptyList();
  }

  public JSExpression getCollection() {
    final ASTNode myNode = getNode();
    ASTNode type = myNode.findChildByType(JSTokenTypes.IN_KEYWORD);
    type = type == null ? myNode.findChildByType(JSTokenTypes.OF_KEYWORD) : type;
    final ASTNode secondExpression = myNode.findChildByType(
      JSElementTypes.EXPRESSIONS, type
    );
    return secondExpression != null ? (JSExpression)secondExpression.getPsi() : null;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2RepeatExpression(this);
    } else {
      super.accept(visitor);
    }
  }
}
