package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
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
public class DartClassReferenceImpl extends DartExpressionImpl implements DartReference {
  public DartClassReferenceImpl(ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement getElement() {
    return this;
  }

  @Override
  public PsiReference getReference() {
    return this;
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
    final DartReference childReference = PsiTreeUtil.getChildOfType(this, DartReference.class);
    if (this instanceof DartParenthesizedExpression) {
      return childReference == null ? DartClassResolveResult.EMPTY : childReference.resolveDartClass();
    }

    if (this instanceof DartArrayAccessExpression) {
      final DartReference reference = PsiTreeUtil.getChildOfType(this, DartReference.class);
      if (reference != null) {
        final DartClassResolveResult resolveResult = reference.resolveDartClass();
        final DartClass resolveResultDartClass = resolveResult.getDartClass();
        if (resolveResultDartClass == null) {
          return resolveResult;
        }
        return DartResolveUtil.getDartClassResolveResult(resolveResultDartClass.findOperator("[]", null),
                                                         resolveResult.getSpecialization());
      }
    }

    if (this instanceof DartAsExpression) {
      return DartResolveUtil.resolveClassByType(((DartAsExpression)this).getType());
    }

    if (this instanceof DartStringLiteralExpression) {
      return DartResolveUtil.findCoreClass(this, "String");
    }

    if (this instanceof DartListLiteralExpression) {
      return DartResolveUtil.findCoreClass(this, "List");
    }

    if (this instanceof DartMapLiteralExpression) {
      return DartResolveUtil.findCoreClass(this, "Map");
    }

    if (this instanceof DartCompoundLiteralExpression) {
      final DartTypeArguments typeArguments = ((DartCompoundLiteralExpression)this).getTypeArguments();
      final DartReference dartReference = PsiTreeUtil.getChildOfType(this, DartReference.class);
      final DartClassResolveResult classResolveResult =
        dartReference == null ? DartClassResolveResult.EMPTY : dartReference.resolveDartClass();
      if (typeArguments == null) {
        return classResolveResult;
      }
      classResolveResult.specializeByParameters(typeArguments);
      return classResolveResult;
    }

    final PsiElement firstChild = getFirstChild();
    if (firstChild instanceof LeafPsiElement) {
      final String literalText = firstChild.getText();
      if ("true".equals(literalText) || "false".equals(literalText)) {
        return DartResolveUtil.findCoreClass(this, "bool");
      }
      else if ("null".equals(literalText)) {
        return DartClassResolveResult.EMPTY;
      }
      else {
        final boolean isFloat = literalText.indexOf('.') != -1 || literalText.indexOf('E') != -1 || literalText.indexOf('e') != -1;
        return DartResolveUtil.findCoreClass(this, isFloat ? "double" : "int");
      }
    }
    return DartClassResolveResult.EMPTY;
  }
}
