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
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.impl.JSCallExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class Angular2FilterExpression extends JSExpressionImpl implements JSCallExpression {
  public Angular2FilterExpression(IElementType elementType) {
    super(elementType);
  }

  public static boolean isFilterNameRef(PsiReference ref, PsiElement parent) {
    return parent instanceof Angular2FilterExpression && ((Angular2FilterExpression)parent).getFilterName() == ref;
  }

  public JSReferenceExpression getFilterName() {
    return (JSReferenceExpression)getFirstChild();
  }

  @Override
  public JSExpression getMethodExpression() {
    final ASTNode node = JSCallExpressionImpl.getMethodExpression(getNode());
    return node != null ? (JSExpression)node.getPsi() : null;
  }

  @Override
  public JSExpression getStubSafeMethodExpression() {
    return null;
  }

  @Nullable
  @Override
  public JSArgumentList getArgumentList() {
    final ASTNode argumentList = JSCallExpressionImpl.getArgumentList(getNode());
    return argumentList != null ? (JSArgumentList)argumentList.getPsi() : null;
  }

  @NotNull
  @Override
  public JSExpression[] getArguments() {
    JSArgumentList argumentList = getArgumentList();
    return argumentList != null ? argumentList.getArguments() : JSExpression.EMPTY_ARRAY;
  }

  @Override
  public boolean isRequireCall() {
    return false;
  }

  @Override
  public boolean isDefineCall() {
    return false;
  }

  @Nullable
  @Override
  public JSElementIndexingData getIndexingData() {
    return null;
  }
}
