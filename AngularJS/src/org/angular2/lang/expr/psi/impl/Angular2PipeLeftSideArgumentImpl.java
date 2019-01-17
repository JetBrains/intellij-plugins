// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSElementImpl;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2PipeArgumentsList;
import org.angular2.lang.expr.psi.Angular2PipeLeftSideArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2PipeLeftSideArgumentImpl extends JSElementImpl implements Angular2PipeLeftSideArgument {

  public Angular2PipeLeftSideArgumentImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public JSExpression getExpression() {
    return doIfNotNull(findChildByType(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS),
                       node -> node.getPsi(JSExpression.class));
  }

  @NotNull
  @Override
  public JSExpression[] getArguments() {
    JSExpression[] mainArgsList = getPipeRightSideExpressions();
    if (mainArgsList != null) {
      return mainArgsList;
    }
    JSExpression thisExpr = getExpression();
    return thisExpr != null
           ? new JSExpression[]{thisExpr}
           : JSExpression.EMPTY_ARRAY;
  }

  @Nullable
  private JSExpression[] getPipeRightSideExpressions() {
    return doIfNotNull(((Angular2PipeExpressionImpl)getParent())
                         .findChildByType(Angular2ElementTypes.PIPE_ARGUMENTS_LIST),
                       node -> doIfNotNull(node.getPsi(Angular2PipeArgumentsList.class),
                                           Angular2PipeArgumentsList::getArguments));
  }

  @Override
  public boolean hasSpreadElement() {
    return false;
  }
}
