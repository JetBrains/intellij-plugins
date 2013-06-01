package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartOperatorExpressionImpl extends DartExpressionImpl implements DartReference {
  public DartOperatorExpressionImpl(ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement getElement() {
    return this;
  }

  @Override
  public PsiReference getReference() {
    return null;
  }

  @Override
  public TextRange getRangeInElement() {
    final TextRange textRange = getTextRange();
    return new TextRange(0, textRange.getEndOffset() - textRange.getStartOffset());
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return this;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return this;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return false;
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  @Override
  public PsiElement resolve() {
    return null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @NotNull
  @Override
  public DartClassResolveResult resolveDartClass() {
    if (this instanceof DartSuffixExpression) {
      return DartResolveUtil.findCoreClass(this, "num");
    }

    final DartReference[] references = PsiTreeUtil.getChildrenOfType(this, DartReference.class);
    if (references == null || references.length == 0) {
      return DartClassResolveResult.EMPTY;
    }
    final DartClassResolveResult leftClassResolveResult = references[0].resolveDartClass();
    final DartClass dartClass = leftClassResolveResult.getDartClass();
    if (dartClass == null) {
      return DartClassResolveResult.EMPTY;
    }
    // ignore right class. it's a warning.
    String sign = getOperatorSign();
    if ("-".equals(sign) && this instanceof DartPrefixExpression) {
      sign = "negate";
    }
    final DartOperator operator = dartClass.findOperator(sign, null);
    return DartResolveUtil.getDartClassResolveResult(operator, leftClassResolveResult.getSpecialization());
  }

  private String getOperatorSign() {
    if (this instanceof DartLogicOrExpression) {
      return "||";
    }
    if (this instanceof DartLogicAndExpression) {
      return "&&";
    }
    if (this instanceof DartCompareExpression) {
      final DartCompareExpression compareExpression = (DartCompareExpression)this;
      final DartEqualityOperator equalityOperator = compareExpression.getEqualityOperator();
      final DartRelationalOperator relationalOperator = compareExpression.getRelationalOperator();
      return DartResolveUtil.getOperatorString(equalityOperator == null ? relationalOperator : equalityOperator);
    }
    if (this instanceof DartBitwiseExpression) {
      return DartResolveUtil.getOperatorString(((DartBitwiseExpression)this).getBitwiseOperator());
    }
    if (this instanceof DartShiftExpression) {
      return DartResolveUtil.getOperatorString(((DartShiftExpression)this).getShiftOperator());
    }
    if (this instanceof DartAdditiveExpression) {
      return DartResolveUtil.getOperatorString(((DartAdditiveExpression)this).getAdditiveOperator());
    }
    if (this instanceof DartMultiplicativeExpression) {
      return DartResolveUtil.getOperatorString(((DartMultiplicativeExpression)this).getMultiplicativeOperator());
    }
    if (this instanceof DartPrefixExpression) {
      final DartPrefixOperator prefixOperator = ((DartPrefixExpression)this).getPrefixOperator();
      return prefixOperator == null ? "" : prefixOperator.getText();
    }
    return "";
  }
}
