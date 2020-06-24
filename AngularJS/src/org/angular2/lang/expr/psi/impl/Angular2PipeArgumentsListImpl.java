// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSArgumentListImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2PipeArgumentsList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2PipeArgumentsListImpl extends JSArgumentListImpl implements Angular2PipeArgumentsList {

  public Angular2PipeArgumentsListImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public JSExpression @NotNull [] getArguments() {
    JSExpression leftSide = getPipeLeftSideExpression();
    return leftSide != null
           ? ArrayUtil.prepend(leftSide, super.getArguments())
           : JSExpression.EMPTY_ARRAY;
  }

  JSExpression @NotNull [] getPipeRightSideExpressions() {
    return super.getArguments();
  }

  private @Nullable JSExpression getPipeLeftSideExpression() {
    return doIfNotNull(((Angular2PipeExpressionImpl)getParent())
                         .findChildByType(Angular2ElementTypes.PIPE_LEFT_SIDE_ARGUMENT),
                       node -> doIfNotNull(node.getPsi(Angular2PipeLeftSideArgumentImpl.class),
                                           Angular2PipeLeftSideArgumentImpl::getPipeLeftSideExpression));
  }

  @Override
  public IElementType getDelimiter() {
    return JSTokenTypes.COLON;
  }
}
