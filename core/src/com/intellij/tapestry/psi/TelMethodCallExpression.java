package com.intellij.tapestry.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: 09.10.2009
 *         Time: 16:29:14
 */
public class TelMethodCallExpression extends TelCompositeElement implements TelReferenceQualifier {

  public TelMethodCallExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  public PsiType[] getArgumentTypes() {
    TelExpression[] args = getArgumentList().getArguments();
    return ContainerUtil.map2Array(args, PsiType.class, (NullableFunction<TelExpression, PsiType>)expression -> expression.getPsiType());
  }

  @NotNull
  public TelArgumentList getArgumentList() {
    return findNotNullChildByClass(TelArgumentList.class);
  }

  @Nullable
  public PsiType getPsiType() {
    return findNotNullChildByClass(TelReferenceExpression.class).getPsiType();
  }
}
