package com.intellij.tapestry.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class TelRangeExpression extends TelCompositeElement implements TelExpression {

  public TelRangeExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public PsiType getPsiType() {
    return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName(CommonClassNames.JAVA_UTIL_LIST, getResolveScope());
  }
}