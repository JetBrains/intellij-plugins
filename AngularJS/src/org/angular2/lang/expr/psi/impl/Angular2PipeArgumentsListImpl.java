// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSArgumentListImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import org.angular2.lang.expr.psi.Angular2PipeArgumentsList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2PipeArgumentsListImpl extends JSArgumentListImpl implements Angular2PipeArgumentsList {

  public Angular2PipeArgumentsListImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  @NotNull
  public JSExpression[] getArguments() {
   return ArrayUtil.prepend(getPipeLeftSideExpression(),  super.getArguments());
  }

  @Nullable
  JSExpression getPipeLeftSideExpression() {
    final ASTNode node = ((Angular2PipeExpressionImpl)getParent())
      .findChildByType(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS);
    return node != null ? node.getPsi(JSExpression.class) : null;
  }

}
