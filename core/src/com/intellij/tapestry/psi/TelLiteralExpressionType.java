package com.intellij.tapestry.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 07.06.2008
 */
public class TelLiteralExpressionType extends TelCompositeElementType {
  private final String myTypeName;
  private final PsiPrimitiveType myPrimitiveType;

  public TelLiteralExpressionType(@NotNull @NonNls String debugName, @NotNull String typeName) {
    super(debugName);
    myTypeName = typeName;
    myPrimitiveType = null;
  }

  public TelLiteralExpressionType(@NotNull @NonNls String debugName, @NotNull PsiType primitiveType) {
    super(debugName);
    myTypeName = null;
    assert primitiveType instanceof PsiPrimitiveType;
    myPrimitiveType = (PsiPrimitiveType)primitiveType;
  }

  @Override
  public PsiElement createPsiElement(ASTNode node) {
    return new TelLiteralExpression(node);
  }

  public class TelLiteralExpression extends TelCompositeElement implements TelExpression {
    public TelLiteralExpression(@NotNull final ASTNode node) {
      super(node);
    }

    public PsiType getPsiType() {
      if (myPrimitiveType != null) {
        return myPrimitiveType;
      }
      return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName(myTypeName, getResolveScope());
    }
  }
}
