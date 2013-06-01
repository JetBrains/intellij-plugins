package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.jetbrains.lang.dart.psi.DartOperator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
abstract public class AbstractDartOperator extends DartPsiCompositeElementImpl implements DartOperator {
  public AbstractDartOperator(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public String getOperatorString() {
    return DartResolveUtil.getOperatorString(getUserDefinableOperator());
  }
}
