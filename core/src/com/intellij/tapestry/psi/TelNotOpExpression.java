package com.intellij.tapestry.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: 09.10.2009
 *         Time: 16:29:14
 */
public class TelNotOpExpression extends TelCompositeElement implements TelExpression {

  public TelNotOpExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Nullable
  public PsiType getPsiType() {
    return PsiType.BOOLEAN;
  }
}