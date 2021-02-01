package com.intellij.tapestry.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class TelMethodCallExpression extends TelCompositeElement implements TelReferenceQualifier {

  public TelMethodCallExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public PsiType @NotNull [] getArgumentTypes() {
    TelExpression[] args = getArgumentList().getArguments();
    return ContainerUtil.map2Array(args, PsiType.class, expression -> expression.getPsiType());
  }

  @NotNull
  public TelArgumentList getArgumentList() {
    return findNotNullChildByClass(TelArgumentList.class);
  }

  @Override
  @Nullable
  public PsiType getPsiType() {
    return findNotNullChildByClass(TelReferenceExpression.class).getPsiType();
  }
}
