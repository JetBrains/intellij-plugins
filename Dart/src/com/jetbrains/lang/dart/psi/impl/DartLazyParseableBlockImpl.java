package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.jetbrains.lang.dart.psi.DartLazyParseableBlock;
import com.jetbrains.lang.dart.psi.DartStatements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartLazyParseableBlockImpl extends DartPsiCompositeElementImpl implements DartLazyParseableBlock {
  public DartLazyParseableBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public DartStatements getStatements() {
    return findChildByClass(DartStatements.class);
  }
}
